package kspt.bank.domain;

import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellSize;
import kspt.bank.domain.entities.Client;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.lang.reflect.Field;
import java.util.stream.IntStream;

import static kspt.bank.domain.CellApplicationInteractorTest.getSomeCorrectPassportInfo;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ConstantConditions")
class VaultTest {
    @ParameterizedTest
    @EnumSource(CellSize.class)
    void testRequestCellOfSize_ShouldReturnRequestedSize(CellSize size) {
        // when
        final Cell cell = Vault.getInstance().requestCell(size);
        // then
        assertThat(cell.getSize()).isEqualTo(size);
    }

    @ParameterizedTest
    @ArgumentsSource(CellSizesWithTotalsProvider.class)
    void testRequestCellOfSize_ShouldReturnNull(CellSize size, int totalCellsOfThatSize) {
        // given
        leaseCells(size, totalCellsOfThatSize);
        // when
        final Cell cell = Vault.getInstance().requestCell(size);
        // then
        assertThat(cell).isNull();
    }

    private void leaseCells(CellSize size, int numberOfCells) {
        IntStream.range(0, numberOfCells).forEach(i ->
                Vault.getInstance().requestCell(size)
                        .setLeaseholder(new Client(i, getSomeCorrectPassportInfo()))
        );
    }

    @Test
    void testRequestCellOfSize_ShouldReturnTheSameCellTwice() {
        // given
        final Cell cell1 = Vault.getInstance().requestCell(CellSize.MEDIUM);
        // when
        final Cell cell2 = Vault.getInstance().requestCell(CellSize.MEDIUM);
        // then
        assertThat(cell1).isEqualTo(cell2);
    }

    @Test
    void testRequestCellOfSize_ShouldReturnTwoDifferentCells() {
        // given
        final Cell cell1 = Vault.getInstance().requestCell(CellSize.MEDIUM);
        cell1.setLeaseholder(new Client(1, getSomeCorrectPassportInfo()));
        // when
        final Cell cell2 = Vault.getInstance().requestCell(CellSize.MEDIUM);
        // then
        assertThat(cell1).isNotEqualTo(cell2);
    }

    @ParameterizedTest
    @ArgumentsSource(CellSizesWithTotalsProvider.class)
    void testGetNumberOfAvailableCells_NoLeased(CellSize size, int totalCellsOfThatSize) {
        // when
        final int cellsNumber = Vault.getInstance().getNumberOfAvailableCells(size);
        // then
        assertThat(cellsNumber).isEqualTo(totalCellsOfThatSize);
    }

    @ParameterizedTest
    @ArgumentsSource(CellSizesWithTotalsProvider.class)
    void testGetNumberOfAvailableCells_SomeLeased(CellSize size, int totalCellsOfThatSize) {
        // given
        Assumptions.assumeTrue(totalCellsOfThatSize > 0);
        leaseCells(size, 1);
        // when
        final int cellsNumber = Vault.getInstance().getNumberOfAvailableCells(size);
        // then
        assertThat(cellsNumber).isEqualTo(totalCellsOfThatSize - 1);
    }

    @BeforeEach
    void resetSingleton()
    throws Exception {
        Field instance = Vault.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }
}
