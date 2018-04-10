package kspt.bank.domain;

import kspt.bank.boundaries.NotificationGate;
import kspt.bank.domain.entities.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.Period;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CellManipulationInteractorTest {
    private final ManipulationLog manipulationLog = mock(ManipulationLog.class);

    private final NotificationGate notificationGate = mock(NotificationGate.class);

    private final CellManipulationInteractor interactor =
            new CellManipulationInteractor(manipulationLog, notificationGate);

    private final Client client = TestDataGenerator.getSampleClient();

    private final Cell cellOne = new Cell(1, CellSize.SMALL);

    private final Cell cellTwo = new Cell(2, CellSize.MEDIUM);

    private final Cell cellThree = new Cell(3, CellSize.BIG);

    private final Precious myPrecious = new Precious(1, "The Ring Of Power");

    private final Precious tooBigPrecious = new Precious(100, "Too Big Precious");

    @Test
    void testGetClientsCells() {
        // given
        Vault.getInstance().getLeasingController().startLeasing(cellOne, client, Period.ofMonths(1));
        Vault.getInstance().getLeasingController().startLeasing(cellTwo, client, Period.ofMonths(2));
        Vault.getInstance().getLeasingController().startLeasing(cellThree, client, Period.ofMonths(3));
        // when
        final List<Cell> clientsCells = interactor.getClientsCells(client);
        // then
        assertThat(clientsCells).containsExactlyInAnyOrder(cellOne, cellTwo, cellThree);
    }

    @Test
    void testPutPrecious() {
        // given
        Vault.getInstance().getLeasingController().startLeasing(cellOne, client, Period.ofMonths(1));
        // when
        interactor.putPrecious(cellOne, myPrecious, client);
        // then
        assertThat(cellOne.getContainedPrecious()).isEqualTo(myPrecious);
        verify(manipulationLog).logPutManipulation(anyString(), eq(client), eq(cellOne));
    }

    @Test
    void testPutPrecious_TooBigPrecious() {
        // given
        Vault.getInstance().getLeasingController().startLeasing(cellThree, client, Period.ofMonths(3));
        // then
        assertThrows(PutManipulationValidator.ManipulationNotAllowed.class, // when
                () -> interactor.putPrecious(cellThree, tooBigPrecious, client));
    }

    @Test
    void testPutPrecious_NotEmptyCell() {
        // given
        Vault.getInstance().getLeasingController().startLeasing(cellTwo, client, Period.ofMonths(2));
        cellThree.setContainedPrecious(myPrecious);
        // then
        assertThrows(PutManipulationValidator.ManipulationNotAllowed.class, // when
                () -> interactor.putPrecious(cellThree, new Precious(1, ""), client));
    }

    @Test
    void testGetPrecious() {
        // given
        cellTwo.setContainedPrecious(myPrecious);
        // when
        final Precious precious = interactor.getPrecious(cellTwo, client);
        // then
        assertThat(precious).isEqualTo(myPrecious);
        verify(manipulationLog).logGetManipulation(anyString(), eq(client), eq(cellTwo));
    }

    @Test
    void testOpenCell() {
        // given
        Assumptions.assumeFalse(Vault.getVaultHardware().isOpened(cellOne));
        // when
        interactor.openCell(cellOne, client);
        // then
        assertTrue(Vault.getVaultHardware().isOpened(cellOne));
        verify(manipulationLog).logEvent(anyString(), eq(client), eq(cellOne));
    }

    @Test
    void testCloseCell() {
        // given
        interactor.openCell(cellOne, client);
        Assumptions.assumeTrue(Vault.getVaultHardware().isOpened(cellOne));
        // when
        interactor.closeCell(cellOne, client);
        // then
        assertFalse(Vault.getVaultHardware().isOpened(cellOne));
        verify(manipulationLog, Mockito.times(2)).logEvent(anyString(), eq(client), eq(cellOne));
    }

    @Test
    void testNotifyAboutManipulation() {
        // when
        interactor.notifyAboutManipulation(cellOne, client);
        // then
        verify(notificationGate).notifyManager(anyString());
    }

    @BeforeEach
    void resetSingleton()
    throws Exception {
        Field instance = Vault.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }
}
