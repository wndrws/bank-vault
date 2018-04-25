package kspt.bank.domain.bp;

import kspt.bank.boundaries.NotificationGate;
import kspt.bank.domain.CellManipulationInteractor;
import kspt.bank.domain.TestDataGenerator;
import kspt.bank.domain.Vault;
import kspt.bank.domain.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.lang.reflect.Field;
import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ManipulationTest extends TestUsingDatabase {
    private final ManipulationLog manipulationLog = mock(ManipulationLog.class);

    private final NotificationGate notificationGate = mock(NotificationGate.class);

    private final CellManipulationInteractor cmInteractor =
            new CellManipulationInteractor(manipulationLog, notificationGate);

    private final RoleClient roleClient = new RoleClient();

    private final RoleOperator roleOperator = new RoleOperator();

    private Cell smallCell;

    private Cell mediumCell;

    private Cell bigCell;

    private Cell cell;

    @ParameterizedTest
    @EnumSource(CellSize.class)
    void testBusinessProcess(final CellSize cellSize) {
        cell = roleClient.selectCell(cellSize);
        assertThatCorrectCellIsReturned(cellSize);

        assertThatCellIsClosed();
        roleOperator.openCell();
        assertThatCellIsOpened();

        assertThatCellIsEmpty();
        roleClient.putPrecious();
        assertThatCellContainsThePrecious();

        final Precious precious = roleClient.getPrecious();
        assertThatCorrectPreciousIsReturned(precious);
        assertThatCellIsEmpty();

        assertThatCellIsOpened();
        roleOperator.closeCell();
        assertThatCellIsClosed();

        roleOperator.notifyManager();
        verifyThatManagerIsNotified();
    }

    private void assertThatCorrectPreciousIsReturned(Precious precious) {
        assertThat(precious).isEqualTo(roleClient.precious);
    }

    private void verifyThatManagerIsNotified() {
        verify(notificationGate).notifyManager(anyString());
    }

    private void assertThatCellIsEmpty() {
        assertTrue(cell.isEmpty());
    }

    private void assertThatCellContainsThePrecious() {
        assertThat(cell.getContainedPrecious()).isEqualTo(roleClient.precious);
    }

    private void assertThatCellIsOpened() {
        assertTrue(Vault.getVaultHardware().isOpened(cell));
    }

    private void assertThatCellIsClosed() {
        assertFalse(Vault.getVaultHardware().isOpened(cell));
    }

    private void assertThatCorrectCellIsReturned(final CellSize selectedCellNum) {
        switch (selectedCellNum) {
            case SMALL: assertThat(cell).isEqualTo(smallCell); break;
            case MEDIUM: assertThat(cell).isEqualTo(mediumCell); break;
            case BIG: assertThat(cell).isEqualTo(bigCell); break;
            default: throw new IllegalArgumentException();
        }
    }

    private class RoleClient {
        final Client client = TestDataGenerator.getSampleClient();

        final Precious precious = new Precious(1, "The Ring Of Power");

        Cell selectCell(CellSize size) {
            return cmInteractor.getClientsCells(client).stream()
                    .filter(cell -> cell.getSize() == size)
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
        }

        void putPrecious() {
            cmInteractor.putPrecious(cell, precious, client);
        }

        Precious getPrecious() {
            return cmInteractor.getPrecious(cell, client);
        }
    }

    private class RoleOperator {
        void openCell() {
            cmInteractor.openCell(cell, roleClient.client);
        }

        void closeCell() {
            cmInteractor.closeCell(cell, roleClient.client);
        }

        void notifyManager() {
            cmInteractor.notifyAboutManipulation(cell, roleClient.client);
        }
    }

    @BeforeEach
    void setUp()
    throws Exception {
        resetSingleton();
        smallCell = Vault.getInstance().requestCell(CellSize.SMALL);
        mediumCell = Vault.getInstance().requestCell(CellSize.MEDIUM);
        bigCell = Vault.getInstance().requestCell(CellSize.BIG);
        Vault.getInstance().getLeasingController()
                .startLeasing(smallCell, roleClient.client, Period.ofMonths(1));
        Vault.getInstance().getLeasingController()
                .startLeasing(mediumCell, roleClient.client, Period.ofMonths(2));
        Vault.getInstance().getLeasingController()
                .startLeasing(bigCell, roleClient.client, Period.ofMonths(3));
    }

    private void resetSingleton()
    throws Exception {
        Field instance = Vault.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }
}
