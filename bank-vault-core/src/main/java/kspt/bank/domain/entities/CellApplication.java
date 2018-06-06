package kspt.bank.domain.entities;

import kspt.bank.dao.AutoIdDomainObject;
import kspt.bank.domain.PriceCalculator;
import kspt.bank.enums.CellApplicationStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Period;

@EqualsAndHashCode(callSuper = false, of = "id")
@Data
public class CellApplication extends AutoIdDomainObject {

    private final Client leaseholder;

    private Cell cell;

    private Period leasePeriod;

    private CellApplicationStatus status;

    public CellApplication(final Client leaseholder) {
        super();
        this.status = CellApplicationStatus.CREATED;
        this.leaseholder = leaseholder;
    }

    public CellApplication(final int id, final Client leaseholder, final Cell cell,
            final Period period, final CellApplicationStatus status) {
        super(id);
        this.status = CellApplicationStatus.CREATED;
        this.leaseholder = leaseholder;
        this.cell = cell;
        this.leasePeriod = period;
        this.status = status;
    }

    public long calculateLeaseCost() {
        return PriceCalculator.getCostOf(cell, leasePeriod.getDays());
    }
}
