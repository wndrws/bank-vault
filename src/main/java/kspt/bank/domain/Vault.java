package kspt.bank.domain;

import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellSize;
import lombok.Synchronized;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class Vault {
    final static int NUMBER_OF_SMALL_CELLS = 20;

    final static int NUMBER_OF_MEDIUM_CELLS = 10;

    final static int NUMBER_OF_BIG_CELLS = 5;

    private static int cellCounter = 0;

    private static volatile Vault instance = null;

    private final EnumMap<CellSize, List<Cell>> cells;

    public static Vault getInstance() {
        // Thread-safe lazy singleton implementation
        if (instance == null) {
            synchronized(Vault.class) {
                if (instance == null) {
                    instance = new Vault();
                }
            }
        }
        return instance;
    }

    private Vault() {
        cells = new EnumMap<>(CellSize.class);
        cells.put(CellSize.SMALL, createCellsOfSize(NUMBER_OF_SMALL_CELLS, CellSize.SMALL));
        cells.put(CellSize.MEDIUM, createCellsOfSize(NUMBER_OF_MEDIUM_CELLS, CellSize.MEDIUM));
        cells.put(CellSize.BIG, createCellsOfSize(NUMBER_OF_BIG_CELLS, CellSize.BIG));
    }

    static private List<Cell> createCellsOfSize(final int numberOfCells, final CellSize cellSize) {
        return IntStream.range(cellCounter, cellCounter + numberOfCells)
                .peek(__ -> cellCounter++) // dirty stream =P
                .mapToObj(id -> new Cell(id, cellSize))
                .collect(Collectors.toList());
    }

    @Synchronized
    public Cell requestCell(final CellSize size) {
        final List<Cell> cellsOfRequestedSize = cells.getOrDefault(size, new ArrayList<>());
        if (cellsOfRequestedSize.size() > 0) {
            return findNotLeasedCell(cellsOfRequestedSize);
        } else {
            return null;
        }
    }

    private static Cell findNotLeasedCell(List<Cell> cellsOfRequestedSize) {
        return cellsOfRequestedSize.stream().filter(Cell::isNotLeased).findAny().orElse(null);
    }

    int getNumberOfAvailableCells(final CellSize size) {
        final List<Cell> cellsOfRequestedSize = cells.getOrDefault(size, new ArrayList<>());
        return (int) cellsOfRequestedSize.stream().filter(Cell::isNotLeased).count();
    }
}
