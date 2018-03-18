package kspt.bank.domain;

import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellSize;
import kspt.bank.domain.entities.PassportInfo;

import java.util.Optional;

public class CellApplicationInteractor {

    public void acceptClientInfo(final PassportInfo clientInfo) {
        ClientPassportValidator.checkValidity(clientInfo);
    }

    public Optional<Cell> requestCellOfSize(final CellSize size) {
        return Vault.getInstance().requestCell(size);
    }
}
