package kspt.bank.dto;

import kspt.bank.enums.CellApplicationStatus;
import kspt.bank.enums.CellSize;
import lombok.Value;

import java.time.LocalDate;

@Value
public final class CellDTO {
    public final String codeName;

    public final CellSize size;

    public final CellApplicationStatus status;

    public final LocalDate leaseBegin;

    public final Integer leaseDays;

    public final String containedPreciousName;

    public final Integer applicationId;
}
