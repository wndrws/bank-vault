package kspt.bank.domain;

import com.google.common.base.Preconditions;
import kspt.bank.domain.entities.Cell;

public class PriceCalculator {
    final static long PRICE_OF_VOLUME_UNIT_PER_MONTH = 50L;

    static long getCostOf(final Cell cell, final int numberOfMonths) {
        Preconditions.checkArgument(numberOfMonths > 0);
        return cell.getSize().getVolume() * PRICE_OF_VOLUME_UNIT_PER_MONTH * numberOfMonths;
    }

    static long discount(final long sum, final int percent) {
        Preconditions.checkArgument(0 <= percent && percent <= 100);
        return Math.round(((double) sum) * (100 - percent) / 100.0);
    }
}
