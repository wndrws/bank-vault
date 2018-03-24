package kspt.bank.domain;

import com.google.common.base.Preconditions;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.boundaries.PaymentGate;
import kspt.bank.domain.entities.*;
import lombok.AllArgsConstructor;

import java.util.Optional;

import static kspt.bank.domain.PriceCalculator.*;

@AllArgsConstructor
public class CellApplicationInteractor {
    private final ClientsRepository clientsRepository;

    private final PaymentGate paymentGate;

    public void acceptClientInfo(final PassportInfo clientInfo)
    throws ClientPassportValidator.IncorrectPassportInfo {
        ClientPassportValidator.checkValidity(clientInfo);
        if (!clientsRepository.containsClientWith(clientInfo)) {
            clientsRepository.addClientWith(clientInfo);
        }
    }

    public Optional<Cell> requestCellOfSize(final CellSize size) {
        final Cell cell = Vault.getInstance().requestCell(size);
        return cell != null ? Optional.of(cell) : Optional.empty();
    }

    public void acceptPayment(final long sum, final PaymentMethod paymentMethod) {
        Preconditions.checkArgument(sum > 0, "Payment must be positive");
        paymentGate.acceptPayment(sum, paymentMethod);
    }

    long calculatePayment(final Cell cell) {
        return calculatePaymentWithDiscount(cell, 0);
    }

    long calculatePaymentWithDiscount(final Cell cell, final int percent) {
        // TODO
        return discount(getCostOf(cell, 1), percent);
    }

    boolean hasGoodCreditHistory(final Client client) {
        // TODO
        return false;
    }
}
