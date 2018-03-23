package kspt.bank.domain;

import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellSize;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PriceCalculatorTest {
    @ParameterizedTest
    @MethodSource("provideLeaseVariants")
    void testGetPriceFor(CellSize size, int numOfMonths) {
        // given
        final long expectedCost =
                PriceCalculator.PRICE_OF_VOLUME_UNIT_PER_MONTH * numOfMonths * size.getVolume();
        // when
        final long cost = PriceCalculator.getCostOf(new Cell(1, size), numOfMonths);
        // then
        assertThat(cost).isEqualTo(expectedCost);
    }

    private static Stream<Arguments> provideLeaseVariants() {
        return Arrays.stream(CellSize.values()).flatMap(size -> Stream.of(
                Arguments.of(size, 1), Arguments.of(size, 2), Arguments.of(size, 3)
        ));
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
