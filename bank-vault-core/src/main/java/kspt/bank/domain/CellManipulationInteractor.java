package kspt.bank.domain;

import com.google.common.base.Preconditions;
import kspt.bank.boundaries.CellsRepository;
import kspt.bank.boundaries.NotificationGate;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.ManipulationLog;
import kspt.bank.domain.entities.Precious;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class CellManipulationInteractor {
    @Autowired
    private final ManipulationLog manipulationLog;

    @Autowired
    private final NotificationGate notificationGate;

    @Autowired
    private final VaultHardware vaultHardware;

    @Autowired
    private final LeasingController leasingController;

    @Autowired
    private final CellsRepository cellsRepository;

    public List<Cell> getClientsCells(final Client client) {
        return leasingController.getCellsAndLeaseholders().entrySet().stream()
                .filter(cellToClient -> cellToClient.getValue().equals(client))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void putPrecious(final Cell cell, final Precious precious, final Client client)
    throws PutManipulationValidator.ManipulationNotAllowed {
        PutManipulationValidator.checkManipulation(cell, precious);
        cell.setContainedPrecious(precious);
        manipulationLog.logPutManipulation("Precious put to cell", client, cell);
    }

    public Precious getPrecious(final Cell cell, final Client client) {
        final Precious precious = cell.getContainedPrecious();
        Preconditions.checkState(precious != null, "No precious in the cell " + cell);
        cell.removeContainedPrecious();
        cellsRepository.saveCell(cell);
        manipulationLog.logGetManipulation("Precious got from cell", client, cell);
        return precious;
    }

    public void openCell(final Cell cell, final Client client) {
        vaultHardware.openCell(cell);
        manipulationLog.logEvent("Cell opened", client, cell);
    }

    public void closeCell(final Cell cell, final Client client) {
        vaultHardware.closeCell(cell);
        manipulationLog.logEvent("Cell closed", client, cell);
    }

    public void notifyAboutManipulation(final Cell cell, final Client client) {
        notificationGate.notifyManager("Client " + client + " manipulated with the cell " + cell);
    }
}
