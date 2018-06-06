package kspt.bank.domain;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import kspt.bank.dao.CellDataMapper;
import kspt.bank.dao.DataMapperRegistry;
import kspt.bank.domain.entities.Cell;
import kspt.bank.enums.CellSize;
import lombok.Getter;
import lombok.Synchronized;

import java.time.Clock;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Vault {
    final static Duration DEFAULT_PENDING_DURATION = Duration.ofMinutes(5);

    @VisibleForTesting
    public static Clock CLOCK = Clock.systemDefaultZone();

    @Getter
    private static VaultHardware vaultHardware;

    private final EnumMap<CellSize, List<Cell>> cells;

    @Getter
    private final LeasingController leasingController;

    private final Set<Cell> pendingCells = Collections.synchronizedSet(new HashSet<>());

    private boolean useDatabase;

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
        final CellDataMapper mapper = (CellDataMapper) DataMapperRegistry.getMapper(Cell.class);
        if (mapper == null) {
            cells = initializeVault();
            leasingController = new LeasingController(CLOCK);
            useDatabase = false;
        } else {
            if (!mapper.findAll().isEmpty()) {
                cells = mapper.findAll().stream().collect(Collectors.groupingBy(
                        Cell::getSize, () -> new EnumMap<>(CellSize.class), Collectors.toList()));
                vaultHardware = new VaultHardware(cells);
                leasingController = new LeasingController(CLOCK, collectLeasingInfo(cells));
            } else {
                cells = initializeVault();
                leasingController = new LeasingController(CLOCK);
            }
            useDatabase = true;
        }
    }

    private Map<Cell, LeasingController.CellLeaseRecord> collectLeasingInfo(
            EnumMap<CellSize, List<Cell>> cells) {
        return cells.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .map(cell -> new AbstractMap.SimpleEntry<>(cell, cell.getCellLeaseRecord()))
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private EnumMap<CellSize, List<Cell>> initializeVault() {
        vaultHardware = new VaultHardware();
        final EnumMap<CellSize, List<Cell>> cells = new EnumMap<>(CellSize.class);
        cells.put(CellSize.SMALL, vaultHardware.getCellsOfSize(CellSize.SMALL));
        cells.put(CellSize.MEDIUM, vaultHardware.getCellsOfSize(CellSize.MEDIUM));
        cells.put(CellSize.BIG, vaultHardware.getCellsOfSize(CellSize.BIG));
        return cells;
    }

    @Synchronized
    public Cell requestCell(final CellSize size) {
        final List<Cell> cellsOfRequestedSize = cells.getOrDefault(size, new ArrayList<>());
        if (cellsOfRequestedSize.size() > 0) {
            return findAvailableCell(cellsOfRequestedSize);
        } else {
            return null;
        }
    }

    Cell requestAnyCell() {
        return Stream.of(CellSize.values()).map(this::requestCell).findFirst().orElse(null);
    }

    private Cell findAvailableCell(List<Cell> cellsOfRequestedSize) {
        return cellsOfRequestedSize.stream().filter(this::isAvailable).findAny().orElse(null);
    }

    int getNumberOfAvailableCells(final CellSize size) {
        final List<Cell> cellsOfRequestedSize = cells.getOrDefault(size, new ArrayList<>());
        return (int) cellsOfRequestedSize.stream().filter(this::isAvailable).count();
    }

    public boolean isAvailable(final Cell cell) {
        return !leasingController.isLeased(cell) && !isPending(cell);
    }

    public Set<Cell> getPendingCells() {
        final CellDataMapper mapper = (CellDataMapper) DataMapperRegistry.getMapper(Cell.class);
        if (useDatabase && mapper != null) {
            return new HashSet<>(mapper.findAllPendingCells());
        } else {
            return pendingCells;
        }
    }

    void pend(final Cell cell, final Duration duration) {
        cell.setPending(true);
        if (!useDatabase) {
            pendingCells.add(cell);
        }
        pendingKeepersPool.submit(() -> {
            try {
                Thread.sleep(duration.toMillis());
                cell.setPending(false);
                if (!useDatabase) {
                    pendingCells.remove(cell);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public boolean isPending(final Cell cell) {
        final CellDataMapper mapper = (CellDataMapper) DataMapperRegistry.getMapper(Cell.class);
        if (useDatabase && mapper != null) {
            return mapper.isPending(cell);
        } else {
            return pendingCells.contains(cell);
        }
    }

    public void stop() {
        pendingKeepersPool.shutdownNow();
        leasingController.stop();
    }
}
