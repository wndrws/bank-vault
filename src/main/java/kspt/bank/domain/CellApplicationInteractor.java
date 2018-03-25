package kspt.bank.domain;

import com.google.common.base.Preconditions;
import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.boundaries.PaymentGate;
import kspt.bank.domain.entities.*;
import lombok.AllArgsConstructor;

import java.time.Period;

@AllArgsConstructor
public class CellApplicationInteractor {
    private final ClientsRepository clientsRepository;

    private final ApplicationsRepository applicationsRepository;

    private final PaymentGate paymentGate;

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

    public long approveApplication(final CellApplication application) {
        Preconditions.checkState(application.getStatus() == CellApplicationStatus.CELL_CHOSEN);
        application.setStatus(CellApplicationStatus.APPROVED);
        application.setLeaseCost(calculatePayment(application));
        return application.getLeaseCost();
    }

    public void acceptPayment(final long sum, final PaymentMethod paymentMethod,
            final CellApplication application) {
        Preconditions.checkState(application.getStatus() == CellApplicationStatus.APPROVED);
        Preconditions.checkArgument(sum > 0 && sum == application.getLeaseCost());
        paymentGate.acceptPayment(sum, paymentMethod);
        application.setStatus(CellApplicationStatus.PAID);
        Vault.getInstance().startLeasing(
                application.getCell(), application.getLeaseholder(), application.getLeasePeriod());
    }

    private long calculatePayment(final CellApplication application) {
        final long fullCost = PriceCalculator.getCostOf(
                application.getCell(), application.getLeasePeriod().getMonths());
        return hasGoodCreditHistory(application.getLeaseholder()) ?
                PriceCalculator.discount(fullCost, 25) : fullCost;
    }

    private static boolean hasGoodCreditHistory(final Client client) {
        // TODO
        return false;
    }
}
