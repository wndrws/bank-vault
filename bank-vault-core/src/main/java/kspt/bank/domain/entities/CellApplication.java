package kspt.bank.domain.entities;

import kspt.bank.domain.PriceCalculator;
import kspt.bank.enums.CellApplicationStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Period;

@EqualsAndHashCode(exclude = "cell")
@Data
public class CellApplication {
    @Id
    @GeneratedValue
    private Integer id;

    @OneToOne(optional = false)
    @JoinColumn(name = "client_id")
    @NotNull
    private Client leaseholder;

    @OneToOne
    @JoinColumn(name = "cell_id")
    private Cell cell;

    @Column(name = "period_y")
    private int leasePeriodYears;

    @Column(name = "period_m")
    private int leasePeriodMonths;

    @Column(name = "period_d")
    private int leasePeriodDays;

    @Enumerated(EnumType.STRING)
    @NotNull
    private CellApplicationStatus status;

    public CellApplication(final Client leaseholder) {
        this.status = CellApplicationStatus.CREATED;
        this.leaseholder = leaseholder;
    }

    public CellApplication(final Client leaseholder, final Cell cell,
            final Period period, final CellApplicationStatus status) {
        this.status = CellApplicationStatus.CREATED;
        this.leaseholder = leaseholder;
        this.cell = cell;
        this.leasePeriodYears = period.getYears();
        this.leasePeriodMonths = period.getMonths();
        this.leasePeriodDays = period.getDays();
        this.status = status;
    }

    public void setLeasePeriod(final Period period) {
        this.leasePeriodYears = period.getYears();
        this.leasePeriodMonths = period.getMonths();
        this.leasePeriodDays = period.getDays();
    }

    public Period getLeasePeriod() {
        return Period.of(leasePeriodYears, leasePeriodMonths, leasePeriodDays);
    }

    public long calculateLeaseCost() {
        return PriceCalculator.getCostOf(cell, leasePeriodDays);
    }
}
