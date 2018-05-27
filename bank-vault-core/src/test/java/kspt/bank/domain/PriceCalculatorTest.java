package kspt.bank.domain;

import kspt.bank.domain.entities.Cell;
import kspt.bank.enums.CellSize;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.assertj.core.api.Assertions.assertThat;

class PriceCalculatorTest {
    @ParameterizedTest
    @ArgumentsSource(LeaseVariantsProvider.class)
    void testGetPriceFor(CellSize size, int numOfMonths) {
        // given
        final long expectedCost =
                PriceCalculator.PRICE_OF_VOLUME_UNIT_PER_MONTH * numOfMonths * size.getVolume();
        // when
        final long cost = PriceCalculator.getCostOf(new Cell(1, size), numOfMonths);
        // then
        assertThat(cost).isEqualTo(expectedCost);
    }

    @Test
    void testDiscount() {
        // given
        final long sum = 1000;
        final int percent = 20;
        final long expectedDiscountedSum = 800;
        // when
        final long discountedSum = PriceCalculator.discount(sum, percent);
        // then
        assertThat(discountedSum).isEqualTo(expectedDiscountedSum);
    }
}
