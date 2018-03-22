package kspt.bank.domain;

import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellSize;
import kspt.bank.domain.entities.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
    @MethodSource("provideCellSizesWithTheirTotals")
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

    @BeforeEach
    void resetSingleton()
    throws Exception {
        Field instance = Vault.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    private static Stream<Arguments> provideCellSizesWithTheirTotals() {
        return Stream.of(
                Arguments.of(CellSize.SMALL, Vault.NUMBER_OF_SMALL_CELLS),
                Arguments.of(CellSize.MEDIUM, Vault.NUMBER_OF_MEDIUM_CELLS),
                Arguments.of(CellSize.BIG, Vault.NUMBER_OF_BIG_CELLS)
        );
    }
}
