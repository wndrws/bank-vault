package kspt.bank.messaging;

import kspt.bank.domain.entities.CellSize;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RequestWithCellChoice extends Request {
    public final CellSize cellSize;
}
