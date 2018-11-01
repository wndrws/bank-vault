package kspt.bank.domain;

import com.google.common.base.Preconditions;
import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.NotificationGate;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellApplication;
import kspt.bank.enums.CellApplicationStatus;
import kspt.bank.domain.entities.Client;
import kspt.bank.external.Invoice;
import kspt.bank.external.PaymentSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;

@Component
public class LeasingControlInteractor {
    private final NotificationGate notificationGate;

    private final ApplicationsRepository applicationsRepository;

    private final PaymentSystem paymentSystem;

    @Autowired
    private Vault vault;

    public LeasingControlInteractor(final Clock clock, final NotificationGate notificationGate,
            final ApplicationsRepository applicationsRepository, final PaymentSystem paymentSystem) {
        Vault.CLOCK = clock;
        this.notificationGate = notificationGate;
        this.applicationsRepository = applicationsRepository;
        this.paymentSystem = paymentSystem;
    }

    public void sendNotifications() {
        vault.getLeasingController().getCellsAndLeaseholders()
                .forEach((cell, leaseholder) -> {
                    if (vault.getLeasingController().isLeasingExpired(cell)) {
                        notificationGate.notifyClientAboutLeasingExpiration(leaseholder, cell);
                    }
                });
    }

    public Invoice continueLeasing(final Client client, final Cell cell, final Period leasePeriod) {
        final CellApplication application = new CellApplication(client);
        application.setStatus(CellApplicationStatus.APPROVED);
        application.setCell(cell);
        application.setLeasePeriod(leasePeriod);
        applicationsRepository.save(application);
        return paymentSystem.issueInvoice(application.calculateLeaseCost(), application.getId());
    }

    public void acceptPayment(final Invoice invoice) {
        final Integer applicationId = paymentSystem.findGood(invoice);
        final CellApplication application = applicationsRepository.find(applicationId);
        Preconditions.checkNotNull(application);
        Preconditions.checkState(application.getStatus() == CellApplicationStatus.APPROVED);
        Preconditions.checkState(invoice.isPaid());
        application.setStatus(CellApplicationStatus.PAID);
        applicationsRepository.save(application);
        vault.getLeasingController().startLeasing(
                application.getCell(), application.getLeaseholder(), application.getLeasePeriod());
    }

    public void stopLeasing(final Client client, final Cell cell) {
        if (!cell.isEmpty()) {
            notificationGate.notifyManagerAboutLeasingEnd(cell);
        } else {
            vault.getLeasingController().endLeasing(cell);
            notificationGate.notifyClient(client, "Your leasing for cell " + cell + " has ended");
        }
    }

    public void arrangeLeasingEnd(final Client client, final LocalDate preciousExtractionDay) {
        notificationGate.notifyClientAboutArrangement(client, "You are invited " +
                "to the Bank Vault to extract your precious", preciousExtractionDay);
    }
}
