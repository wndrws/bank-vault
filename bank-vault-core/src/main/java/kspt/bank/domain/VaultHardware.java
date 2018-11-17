package kspt.bank.domain;

import com.google.common.collect.ImmutableSet;
import kspt.bank.domain.entities.Cell;
import kspt.bank.enums.CellSize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
public class VaultHardware {
    final static int NUMBER_OF_SMALL_CELLS = 20;

    final static int NUMBER_OF_MEDIUM_CELLS = 10;

    final static int NUMBER_OF_BIG_CELLS = 5;

    private static int CURRENT_CELL_ID = 0;

    private final ImmutableSet<CellHardware> cells;

    public VaultHardware() {
        log.warn("Creating new cells as the Vault is empty...");
        final List<CellHardware> smallCells = createCellsOfSize(NUMBER_OF_SMALL_CELLS, CellSize.SMALL);
        final List<CellHardware> mediumCells = createCellsOfSize(NUMBER_OF_MEDIUM_CELLS, CellSize.MEDIUM);
        final List<CellHardware> bigCells = createCellsOfSize(NUMBER_OF_BIG_CELLS, CellSize.BIG);
        cells = Stream.of(smallCells, mediumCells, bigCells).flatMap(Collection::stream)
                .collect(ImmutableSet.toImmutableSet());
    }

    public VaultHardware(final EnumMap<CellSize, List<Cell>> cellsEntitiesBySize) {
        log.warn("Loading cells from the database...");
        cells = cellsEntitiesBySize.entrySet().stream().flatMap(entry -> entry.getValue().stream())
                .map(cell -> new CellHardware(cell.getId(), cell.getSize(), cell))
                .collect(ImmutableSet.toImmutableSet());
    }

    static private List<CellHardware> createCellsOfSize(final int numberOfCells, final CellSize cellSize) {
        return IntStream.range(0, numberOfCells)
                .map(__ -> CURRENT_CELL_ID++)
                .mapToObj(i -> new CellHardware(i, cellSize, new Cell(i, cellSize)))
                .collect(Collectors.toList());
    }

    List<Cell> getCellsOfSize(final CellSize size) {
        return cells.stream()
                .filter(cellHardware -> cellHardware.getSize() == size)
                .map(CellHardware::getCellRef)
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
    @RequiredArgsConstructor
    private static class CellHardware {
        private final int id;

        private boolean opened = false;

        private final CellSize size;

        private final Cell cellRef;

        void open() {
            opened = true;
        }

        void close() {
            opened = false;
        }
    }
}
