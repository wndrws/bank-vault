package kspt.bank.domain;

import com.google.common.base.Preconditions;
import kspt.bank.boundaries.ClientsBase;
import kspt.bank.domain.entities.*;
import lombok.AllArgsConstructor;

import java.util.Optional;

import static kspt.bank.domain.PriceCalculator.*;

@AllArgsConstructor
public class CellApplicationInteractor {

    private final ClientsBase clientsBase;

    public void acceptClientInfo(final PassportInfo clientInfo) {
        ClientPassportValidator.checkValidity(clientInfo);
        if (!clientsBase.containsClientWith(clientInfo)) {
            clientsBase.addClientWith(clientInfo);
        }
    }

    public Optional<Cell> requestCellOfSize(final CellSize size) {
        final Cell cell = Vault.getInstance().requestCell(size);
        return cell != null ? Optional.of(cell) : Optional.empty();
    }

    public void acceptPayment(final long sum, final PaymentMethod paymentMethod) {
        Preconditions.checkArgument(sum > 0, "Payment must be positive");
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
