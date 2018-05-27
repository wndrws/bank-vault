package kspt.bank.dto;

import com.google.common.collect.Range;
import kspt.bank.enums.CellApplicationStatus;
import kspt.bank.enums.CellSize;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.Period;

@AllArgsConstructor
public final class CellDTO {
    public final String codeName;

    public final CellSize size;

    public final CellApplicationStatus status;

    public final LocalDate leaseBegin;

    public final Period leasePeriod;

    public final String containedPreciousName;
}
