package kspt.bank.domain;

import kspt.bank.domain.CellManipulationInteractor;
import kspt.bank.domain.TestDataGenerator;
import kspt.bank.domain.Vault;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellSize;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.Precious;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Period;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CellManipulationInteractorTest {
    private CellManipulationInteractor interactor = new CellManipulationInteractor();

    private final Client client = TestDataGenerator.getSampleClient();

    private final Cell cellOne = new Cell(1, CellSize.SMALL);

    private final Cell cellTwo = new Cell(2, CellSize.MEDIUM);

    private final Cell cellThree = new Cell(3, CellSize.BIG);

    private final Precious myPrecious = new Precious(1, "The Ring Of Power");

    private final Precious tooBigPrecious = new Precious(100, "Too Big Precious");

    @Test
    void testGetClientsCells() {
        // given
        Vault.getInstance().startLeasing(cellOne, client, Period.ofMonths(1));
        Vault.getInstance().startLeasing(cellTwo, client, Period.ofMonths(2));
        Vault.getInstance().startLeasing(cellThree, client, Period.ofMonths(3));
        // when
        final List<Cell> clientsCells = interactor.getClientsCells(client);
        // then
        assertThat(clientsCells).containsExactlyInAnyOrder(cellOne, cellTwo, cellThree);
    }

    @Test
    void testPutPrecious_ShouldNotThrow() {
        // given
        Vault.getInstance().startLeasing(cellOne, client, Period.ofMonths(1));
        // when
        interactor.putPrecious(cellOne, myPrecious);
        // then
        assertThat(cellOne.getContainedPrecious()).isEqualTo(myPrecious);
    }

    @Test
    void testPutPrecious_ShouldThrow() {
        // given
        Vault.getInstance().startLeasing(cellThree, client, Period.ofMonths(3));
        // then
        assertThrows(PutManipulationValidator.ManipulationNotAllowed.class, // when
                () -> interactor.putPrecious(cellThree, tooBigPrecious));
    }

    @BeforeEach
    void resetSingleton()
    throws Exception {
        Field instance = Vault.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }
}
