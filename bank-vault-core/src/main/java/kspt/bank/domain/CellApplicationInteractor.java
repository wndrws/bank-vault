package kspt.bank.domain;

import com.google.common.base.Preconditions;
import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellApplication;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.PassportInfo;
import kspt.bank.enums.CellApplicationStatus;
import kspt.bank.enums.CellSize;
import kspt.bank.external.Invoice;
import kspt.bank.external.PaymentSystem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Period;

@Component
@AllArgsConstructor
public class CellApplicationInteractor {
    @Autowired
    private final ClientsRepository clientsRepository;

    @Getter
    @Autowired
    private final ApplicationsRepository applicationsRepository;

    @Autowired
    private final PaymentSystem paymentSystem;

    @Autowired
    private final Vault vault;

    @Autowired
    private final LeasingController leasingController;

    public CellApplication createApplication(final PassportInfo passportInfo, final String phone,
            final String email)
    throws ClientPassportValidator.IncorrectPassportInfo {
        ClientPassportValidator.checkValidity(passportInfo);
        final CellApplication newApplication =
                new CellApplication(getOrCreateClient(passportInfo, phone, email));
        applicationsRepository.save(newApplication);
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
        final Cell cell = vault.requestCell(size);
        if (cell == null) {
            return false;
        } else {
            application.setCell(cell);
            application.setLeasePeriod(period);
            application.setStatus(CellApplicationStatus.CELL_CHOSEN);
            applicationsRepository.save(application);
            vault.pend(cell, Vault.DEFAULT_PENDING_DURATION);
            return true;
        }
    }

    public Invoice approveApplication(final CellApplication application) {
        Preconditions.checkState(application.getStatus() == CellApplicationStatus.CELL_CHOSEN);
        final long leaseCost = calculatePayment(application);
        final Invoice invoice = paymentSystem.issueInvoice(leaseCost, application.getId());
        application.setStatus(CellApplicationStatus.APPROVED);
        applicationsRepository.save(application);
        return invoice;
    }

    public void acceptPayment(final Invoice invoice) {
        final Integer applicationId = paymentSystem.findGood(invoice);
        final CellApplication application = applicationsRepository.find(applicationId);
        Preconditions.checkNotNull(application);
        Preconditions.checkState(application.getStatus() == CellApplicationStatus.APPROVED);
        Preconditions.checkState(invoice.isPaid());
        application.setStatus(CellApplicationStatus.PAID);
        applicationsRepository.save(application);
        leasingController.startLeasing(
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
