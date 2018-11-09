package kspt.bank.boundaries;

import kspt.bank.domain.entities.Cell;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CellsRepository {
    Cell findCell(int id);

    List<Cell> findAllCells();

    List<Cell> findAllPendingCells();

    boolean isPending(@Param("cell") final Cell cell);

    Cell saveCell(Cell cell);
}