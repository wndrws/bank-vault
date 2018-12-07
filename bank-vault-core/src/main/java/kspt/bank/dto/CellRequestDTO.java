package kspt.bank.dto;

import kspt.bank.enums.CellSize;
import lombok.Value;

@Value
public class CellRequestDTO {
    public final CellSize size;

    public final Integer leaseDays;
}
