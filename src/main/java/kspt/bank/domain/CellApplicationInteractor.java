package kspt.bank.domain;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.external.Invoice;
import kspt.bank.external.PaymentGate;
import kspt.bank.domain.entities.*;

import java.time.Period;
import java.util.HashMap;
import java.util.Map;

public class CellApplicationInteractor {
    private final ClientsRepository clientsRepository;

    private final ApplicationsRepository applicationsRepository;

    private final PaymentGate paymentGate;

    private final Map<Invoice, CellApplication> invoiceToApplicationMap;

    public CellApplicationInteractor(final ClientsRepository clientsRepository,
            final ApplicationsRepository applicationsRepository, PaymentGate paymentGate) {
        this(clientsRepository, applicationsRepository, paymentGate, new HashMap<>());
    }

    @VisibleForTesting
    CellApplicationInteractor(final ClientsRepository clientsRepository,
            final ApplicationsRepository applicationsRepository, PaymentGate paymentGate,
            final Map<Invoice, CellApplication> invoiceToApplicationMap) {
        this.clientsRepository = clientsRepository;
        this.applicationsRepository = applicationsRepository;
        this.paymentGate = paymentGate;
        this.invoiceToApplicationMap = invoiceToApplicationMap;
    }

    public CellApplication createApplication(final PassportInfo passportInfo, final String phone,
            final String email) // TODO ? split implementations for existing and new client
    throws ClientPassportValidator.IncorrectPassportInfo {
        ClientPassportValidator.checkValidity(passportInfo);
        final CellApplication newApplication =
                new CellApplication(getOrCreateClient(passportInfo, phone, email));
        applicationsRepository.add(newApplication);
        return newApplication;
    }

    private Client getOrCreateClient(PassportInfo passportInfo, String phone, String email) {
        Client client;
        if (!clientsRepository.containsClientWith(passportInfo)) {
            client = new Client(passportInfo, phone, email);
            clientsRepository.add(client);
        } else {
            client = clientsRepository.getClientWith(passportInfo);
        }
        return client;
    }

    public boolean requestCell(final CellSize size, final Period period,
            final CellApplication application) {
        Preconditions.checkState(application.getStatus() == CellApplicationStatus.CREATED);
        final Cell cell = Vault.getInstance().requestCell(size);
        if (cell == null) {
            return false;
        } else {
            application.setCell(cell);
            application.setLeasePeriod(period);
            application.setStatus(CellApplicationStatus.CELL_CHOSEN);
            Vault.getInstance().pend(cell, Vault.DEFAULT_PENDING_DURATION);
            return true;
        }
    }

    public Invoice approveApplication(final CellApplication application) {
        Preconditions.checkState(application.getStatus() == CellApplicationStatus.CELL_CHOSEN);
        final long leaseCost = calculatePayment(application);
        final Invoice invoice = paymentGate.issueInvoice(leaseCost);
        invoiceToApplicationMap.put(invoice, application);
        application.setStatus(CellApplicationStatus.APPROVED);
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

    private long calculatePayment(final CellApplication application) {
        final long fullCost = application.calculateLeaseCost();
        return hasGoodCreditHistory(application.getLeaseholder()) ?
                PriceCalculator.discount(fullCost, 25) : fullCost;
    }

    private static boolean hasGoodCreditHistory(final Client client) {
        // TODO
        return false;
    }
}
