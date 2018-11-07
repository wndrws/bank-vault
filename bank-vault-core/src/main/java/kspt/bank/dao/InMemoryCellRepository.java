package kspt.bank.dao;

import kspt.bank.boundaries.CellsRepository;
import kspt.bank.domain.entities.Cell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class InMemoryCellRepository implements CellsRepository {
    private static AtomicInteger ID_COUNTER = new AtomicInteger(0);

    private final List<Cell> cells = Collections.synchronizedList(new ArrayList<>());

    @Override
    public Cell find(final int id) {
        return cells.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Cell> findAll() {
        return cells;
    }

    @Override
    public List<Cell> findAllPendingCells() {
        return cells.stream()
                .filter(Cell::isPending)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isPending(final Cell cell) {
        return cells.stream()
                .filter(c -> c.getId() == cell.getId())
                .findFirst()
                .map(Cell::isPending)
                .orElse(false);
    }

    @Override
    public Cell save(final Cell cell) {
        cells.removeIf(c -> c.getId() == cell.getId());
        if (cell.getId() == 0) {
            cell.setId(ID_COUNTER.getAndIncrement());
        }
        cells.add(cell);
        return cell;
    }
}
