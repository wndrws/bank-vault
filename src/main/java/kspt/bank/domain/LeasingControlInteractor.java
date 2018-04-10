package kspt.bank.domain;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.NotificationGate;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellApplication;
import kspt.bank.domain.entities.CellApplicationStatus;
import kspt.bank.domain.entities.Client;
import kspt.bank.external.Invoice;
import kspt.bank.external.PaymentGate;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

public class LeasingControlInteractor {
    private final NotificationGate notificationGate;

    private final ApplicationsRepository applicationsRepository;

    private final PaymentGate paymentGate;

    @Getter(AccessLevel.PACKAGE) @VisibleForTesting
    private final Map<Invoice, CellApplication> invoiceToApplicationMap = new HashMap<>();

    public LeasingControlInteractor(final Clock clock, final NotificationGate notificationGate,
            final ApplicationsRepository applicationsRepository, final PaymentGate paymentGate) {
        Vault.CLOCK = clock;
        this.notificationGate = notificationGate;
        this.applicationsRepository = applicationsRepository;
        this.paymentGate = paymentGate;
    }

    public void sendNotifications() {
        Vault.getInstance().getLeasingController().getCellsAndLeaseholders()
                .forEach((cell, leaseholder) -> {
                    if (Vault.getInstance().getLeasingController().isLeasingExpired(cell)) {
                        notificationGate.notifyClientAboutLeasingExpiration(leaseholder, cell);
                    }
                });
    }

    public Invoice continueLeasing(final Client client, final Cell cell, final Period leasePeriod) {
        final CellApplication application = new CellApplication(client);
        application.setStatus(CellApplicationStatus.APPROVED);
        application.setCell(cell);
        application.setLeasePeriod(leasePeriod);
        applicationsRepository.add(application);
        final long leaseCost = application.calculateLeaseCost();
        final Invoice invoice = paymentGate.issueInvoice(leaseCost);
        invoiceToApplicationMap.put(invoice, application);
        return invoice;
    }

    public void acceptPayment(final Invoice invoice) {
        final CellApplication application = invoiceToApplicationMap.get(invoice);
        Preconditions.checkNotNull(application);
        Preconditions.checkState(application.getStatus() == CellApplicationStatus.APPROVED);
        Preconditions.checkState(invoice.isPaid());
        application.setStatus(CellApplicationStatus.PAID);
        Vault.getInstance().getLeasingController().startLeasing(
                application.getCell(), application.getLeaseholder(), application.getLeasePeriod());
    }

    public void stopLeasing(final Client client, final Cell cell) {
        if (!cell.isEmpty()) {
            notificationGate.notifyManagerAboutLeasingEnd(cell);
        } else {
            Vault.getInstance().getLeasingController().endLeasing(cell);
            notificationGate.notifyClient(client, "Your leasing for cell " + cell + " has ended");
        }
    }

    public void arrangeLeasingEnd(final Client client, final LocalDate preciousExtractionDay) {
        notificationGate.notifyClientAboutArrangement(client, "You are invited " +
                "to the Bank Vault to extract your precious", preciousExtractionDay);
    }
}
