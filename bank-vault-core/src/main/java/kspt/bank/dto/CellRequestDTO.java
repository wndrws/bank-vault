package kspt.bank.dto;

import kspt.bank.enums.CellSize;
import lombok.Value;

import java.time.Period;

@Value
public class CellRequestDTO {
    public final CellSize size;

    public final Period leasePeriod;
}
