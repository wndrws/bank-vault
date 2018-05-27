package kspt.bank.domain;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellSize;
import lombok.Getter;
import lombok.Synchronized;

import java.time.Clock;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public final class Vault {
    final static Duration DEFAULT_PENDING_DURATION = Duration.ofMinutes(5);

    @VisibleForTesting
    public static Clock CLOCK = Clock.systemDefaultZone();

    @Getter
    private static VaultHardware vaultHardware;

    private final EnumMap<CellSize, List<Cell>> cells;

    @Getter
    private final LeasingController leasingController = new LeasingController(CLOCK);

    private final Set<Cell> pendingCells = Collections.synchronizedSet(new HashSet<>());

    private final ExecutorService pendingKeepersPool = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("vault-pendingKeepersPool-%d").build());

    private static volatile Vault instance = null;

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
        vaultHardware = new VaultHardware();
        cells = new EnumMap<>(CellSize.class);
        cells.put(CellSize.SMALL, vaultHardware.getCellsOfSize(CellSize.SMALL));
        cells.put(CellSize.MEDIUM, vaultHardware.getCellsOfSize(CellSize.MEDIUM));
        cells.put(CellSize.BIG, vaultHardware.getCellsOfSize(CellSize.BIG));
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

    public boolean isAvailable(final Cell cell) {
        return !leasingController.isLeased(cell) && !pendingCells.contains(cell);
    }

    void pend(final Cell cell, final Duration duration) {
        pendingCells.add(cell);
        pendingKeepersPool.submit(() -> {
            try {
                Thread.sleep(duration.toMillis());
                pendingCells.remove(cell);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public void stop() {
        pendingKeepersPool.shutdownNow();
        leasingController.stop();
    }
}
