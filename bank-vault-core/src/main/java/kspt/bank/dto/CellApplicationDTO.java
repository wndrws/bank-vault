package kspt.bank.dto;

import kspt.bank.enums.CellApplicationStatus;
import lombok.Value;

import java.time.Period;

@Value
public class CellApplicationDTO {
    public final Integer id;

    public final CellDTO cell;

    public final ClientDTO leaseholder;

    public final Period leasePeriod;

    public final CellApplicationStatus status;

    public final Long leaseCost;
}
