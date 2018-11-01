package kspt.bank.domain;

import kspt.bank.domain.entities.Cell;
import kspt.bank.enums.CellSize;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.time.Period;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SuppressWarnings("ConstantConditions")
class VaultTest {
    @Autowired
    private Vault vault;

    @ParameterizedTest
    @EnumSource(CellSize.class)
    void testRequestCellOfSize_ShouldReturnRequestedSize(CellSize size) {
        // when
        final Cell cell = vault.requestCell(size);
        // then
        assertThat(cell.getSize()).isEqualTo(size);
    }

    @ParameterizedTest
    @ArgumentsSource(CellSizesWithTotalsProvider.class)
    void testRequestCellOfSize_ShouldReturnNull(CellSize size, int totalCellsOfThatSize) {
        // given
        leaseCells(size, totalCellsOfThatSize);
        // when
        final Cell cell = vault.requestCell(size);
        // then
        assertThat(cell).isNull();
    }

    private void leaseCells(CellSize size, int numberOfCells) {
        IntStream.range(0, numberOfCells).forEach(__ ->
                startLeasingForSomeClient(vault.requestCell(size)));
    }

    private void startLeasingForSomeClient(final Cell cell) {
        vault.getLeasingController()
                .startLeasing(cell, TestDataGenerator.getSampleClient(), Period.ofMonths(1));
    }

    @Test
    void testRequestCellOfSize_ShouldReturnTheSameCellTwice() {
        // given
        final Cell cell1 = vault.requestCell(CellSize.MEDIUM);
        // when
        final Cell cell2 = vault.requestCell(CellSize.MEDIUM);
        // then
        assertThat(cell1).isEqualTo(cell2);
    }

    @Test
    void testRequestCellOfSize_ShouldReturnTwoDifferentCells() {
        // given
        final Cell cell1 = vault.requestCell(CellSize.MEDIUM);
        startLeasingForSomeClient(cell1);
        // when
        final Cell cell2 = vault.requestCell(CellSize.MEDIUM);
        // then
        assertThat(cell1).isNotEqualTo(cell2);
    }

    @ParameterizedTest
    @ArgumentsSource(CellSizesWithTotalsProvider.class)
    void testGetNumberOfAvailableCells_NoLeased(CellSize size, int totalCellsOfThatSize) {
        // when
        final int cellsNumber = vault.getNumberOfAvailableCells(size);
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
        final int cellsNumber = vault.getNumberOfAvailableCells(size);
        // then
        assertThat(cellsNumber).isEqualTo(totalCellsOfThatSize - 1);
    }
}
