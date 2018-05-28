package kspt.bank.dto;

import kspt.bank.enums.CellApplicationStatus;
import kspt.bank.enums.CellSize;
import lombok.Value;

import java.time.LocalDate;
import java.time.Period;

@Value
public final class CellDTO {
    public final String codeName;

    public final CellSize size;

    public final CellApplicationStatus status;

    public final LocalDate leaseBegin;

    public final Period leasePeriod;

    public final String containedPreciousName;
}
