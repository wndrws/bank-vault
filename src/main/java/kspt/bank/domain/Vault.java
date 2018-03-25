package kspt.bank.domain;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellSize;
import kspt.bank.domain.entities.Client;
import lombok.Synchronized;
import lombok.Value;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class Vault {
    final static int NUMBER_OF_SMALL_CELLS = 20;

    final static int NUMBER_OF_MEDIUM_CELLS = 10;

    final static int NUMBER_OF_BIG_CELLS = 5;

    final static Duration DEFAULT_PENDING_DURATION = Duration.ofMinutes(5);

    private static int cellCounter = 0;

    private static volatile Vault instance = null;

    private final EnumMap<CellSize, List<Cell>> cells;

    private final Map<Cell, CellLeaseRecord> leasingInfo = new HashMap<>();

    private final Set<Cell> pendingCells = Collections.synchronizedSet(new HashSet<>());

    private final ExecutorService pendingKeepersPool = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("vault-pendingKeepersPool-%d").build());

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

    Cell requestAnyCell() {
        return Stream.of(CellSize.values()).map(this::requestCell).findFirst().orElse(null);
    }

    private Cell findNotLeasedCell(List<Cell> cellsOfRequestedSize) {
        return cellsOfRequestedSize.stream().filter(this::isAvailable).findAny().orElse(null);
    }

    int getNumberOfAvailableCells(final CellSize size) {
        final List<Cell> cellsOfRequestedSize = cells.getOrDefault(size, new ArrayList<>());
        return (int) cellsOfRequestedSize.stream().filter(this::isAvailable).count();
    }

    public void startLeasing(final Cell cell, final Client leaseholder, final Period period) {
        final LocalDate today = LocalDate.now();
        leasingInfo.put(cell, new CellLeaseRecord(leaseholder, today, today.plus(period)));
    }

    public void endLeasing(final Cell cell) {
        leasingInfo.remove(cell);
    }

    public boolean isLeased(final Cell cell) {
        return leasingInfo.containsKey(cell);
    }

    public boolean isAvailable(final Cell cell) {
        return !isLeased(cell) && !pendingCells.contains(cell);
    }

    public void pend(final Cell cell, final Duration duration) {
        pendingCells.add(cell);
        pendingKeepersPool.submit(() -> {
            try {
                Thread.sleep(duration.toMillis());
                pendingCells.remove(cell);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Value
    private static class CellLeaseRecord {
        private final Client leaseholder;

        private final LocalDate leaseBegin;

        private final LocalDate leaseEnd;
    }
}
