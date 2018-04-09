package kspt.bank.domain;

import com.google.common.base.Preconditions;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.Precious;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CellManipulationInteractor {
    public List<Cell> getClientsCells(final Client client) {
        return Vault.getInstance().getCellsAndLeaseholders().entrySet().stream()
                .filter(cellToClient -> cellToClient.getValue().equals(client))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void putPrecious(final Cell cell, final Precious precious)
    throws PutManipulationValidator.ManipulationNotAllowed {
        PutManipulationValidator.checkManipulation(cell, precious);
        cell.setContainedPrecious(precious);
    }

    public Precious getPrecious(final Cell cell) {
        final Precious precious = cell.getContainedPrecious();
        Preconditions.checkState(precious != null, "No precious in the cell " + cell);
        cell.removeContainedPrecious();
        return precious;
    }
}
