package kspt.bank.domain.entities;

import kspt.bank.domain.PriceCalculator;
import lombok.Data;

import java.time.Period;

@Data
public class CellApplication {
    private static long currentId = 0L;

    private final long id;

    private final Client leaseholder;

    private Cell cell;

    private Period leasePeriod;

    private CellApplicationStatus status;

    public CellApplication(final Client leaseholder) {
        this.id = currentId++;
        this.status = CellApplicationStatus.CREATED;
        this.leaseholder = leaseholder;
    }

    public long calculateLeaseCost() {
        return PriceCalculator.getCostOf(cell, leasePeriod.getMonths());
    }
}
