package kspt.bank.domain;

import com.google.common.collect.ImmutableSet;
import kspt.bank.domain.entities.Cell;
import kspt.bank.enums.CellSize;
import lombok.Getter;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class VaultHardware {
    final static int NUMBER_OF_SMALL_CELLS = 20;

    final static int NUMBER_OF_MEDIUM_CELLS = 10;

    final static int NUMBER_OF_BIG_CELLS = 5;

    private final ImmutableSet<CellHardware> cells;

    public VaultHardware() {
        final List<CellHardware> smallCells = createCellsOfSize(NUMBER_OF_SMALL_CELLS, CellSize.SMALL);
        final List<CellHardware> mediumCells = createCellsOfSize(NUMBER_OF_MEDIUM_CELLS, CellSize.MEDIUM);
        final List<CellHardware> bigCells = createCellsOfSize(NUMBER_OF_BIG_CELLS, CellSize.BIG);
        cells = Stream.of(smallCells, mediumCells, bigCells).flatMap(Collection::stream)
                .collect(ImmutableSet.toImmutableSet());
    }

    public VaultHardware(final EnumMap<CellSize, List<Cell>> cellsEntitiesBySize) {
        cells = cellsEntitiesBySize.entrySet().stream().flatMap(entry -> entry.getValue().stream())
                .map(cell -> new CellHardware(cell.getId(), cell.getSize()))
                .collect(ImmutableSet.toImmutableSet());
    }

    static private List<CellHardware> createCellsOfSize(final int numberOfCells, final CellSize cellSize) {
        return IntStream.range(0, numberOfCells)
                .mapToObj(__ -> new CellHardware(cellSize))
                .collect(Collectors.toList());
    }

    List<Cell> getCellsOfSize(final CellSize size) {
        return cells.stream()
                .filter(cellHardware -> cellHardware.getSize() == size)
                .map(cellHardware -> new Cell(cellHardware.getSize()))
                .collect(Collectors.toList());
    }

    void openCell(final Cell cell) {
        cells.stream()
                .filter(cellHardware -> cellHardware.getId() == cell.getId())
                .findFirst()
                .ifPresent(CellHardware::open);
    }

    void closeCell(final Cell cell) {
        cells.stream()
                .filter(cellHardware -> cellHardware.getId() == cell.getId())
                .findFirst()
                .ifPresent(CellHardware::close);
    }

    public boolean isOpened(final Cell cell) {
        return cells.stream()
                .filter(cellHardware -> cellHardware.getId() == cell.getId())
                .findFirst()
                .map(CellHardware::isOpened)
                .orElseThrow(IllegalStateException::new);
    }

    @Getter
    private static class CellHardware {
        private static int currentId;

        private int id;

        private boolean opened = false;

        private final CellSize size;

        CellHardware(CellSize size) {
            id = ++currentId;
            this.size = size;
        }

        CellHardware(int id, CellSize size) {
            this.id = id;
            this.size = size;
        }

        void open() {
            opened = true;
        }

        void close() {
            opened = false;
        }
    }
}
