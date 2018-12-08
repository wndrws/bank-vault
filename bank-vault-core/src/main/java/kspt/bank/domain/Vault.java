package kspt.bank.domain;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.CellsRepository;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellApplication;
import kspt.bank.enums.CellApplicationStatus;
import kspt.bank.enums.CellSize;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@Component
@Slf4j
@RequiredArgsConstructor
public final class Vault implements Closeable {
    final static Duration DEFAULT_PENDING_DURATION = Duration.ofMinutes(30);

    @Autowired
    private final VaultHardware vaultHardware;

    @Autowired
    private final CellsRepository cellsRepository;

    @Autowired
    private final ApplicationsRepository applicationsRepository;

    @Autowired
    private final LeasingController leasingController;

    private EnumMap<CellSize, List<Cell>> cells;

    private final ExecutorService pendingKeepersPool = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("vault-pendingKeepersPool-%d").build());

    @PostConstruct
    private void initializeVault() {
        cells = new EnumMap<>(CellSize.class);
        cells.put(CellSize.SMALL, vaultHardware.getCellsOfSize(CellSize.SMALL));
        cells.put(CellSize.MEDIUM, vaultHardware.getCellsOfSize(CellSize.MEDIUM));
        cells.put(CellSize.BIG, vaultHardware.getCellsOfSize(CellSize.BIG));
        cells.values().stream().flatMap(Collection::stream).forEach(cellsRepository::saveCell);
    }

    @Synchronized
    public Cell requestCell(final CellSize size) {
        final List<Cell> cellsOfRequestedSize = cells.getOrDefault(size, new ArrayList<>());
        return cellsOfRequestedSize.isEmpty() ? null : findAvailableCell(cellsOfRequestedSize);
    }

    private Cell findAvailableCell(List<Cell> cellsOfRequestedSize) {
        return cellsOfRequestedSize.stream().filter(this::isAvailable).findAny().orElse(null);
    }

    public boolean isAvailable(final Cell cell) {
        return !leasingController.isLeased(cell) && !isPending(cell);
    }

    Cell requestAnyCell() {
        return Stream.of(CellSize.values()).map(this::requestCell).findFirst().orElse(null);
    }

    int getNumberOfAvailableCells(final CellSize size) {
        final List<Cell> cellsOfRequestedSize = cells.getOrDefault(size, new ArrayList<>());
        return (int) cellsOfRequestedSize.stream().filter(this::isAvailable).count();
    }

    void pend(final Cell cell, final Duration duration) {
        cell.setPending(true);
        cellsRepository.saveCell(cell);
        pendingKeepersPool.submit(() -> {
            try {
                log.info("Cell {} is pending until {}", cell.getId(), LocalTime.now().plus(duration));
                Thread.sleep(duration.toMillis());
                final Cell refreshedCell = cellsRepository.findCell(cell.getId());
                refreshedCell.setPending(false);
                cellsRepository.saveCell(refreshedCell);
                log.info("Cell {} is no more pending", refreshedCell.getId());
                final CellApplication app = applicationsRepository.findByCell(refreshedCell);
                if (app.getStatus() != CellApplicationStatus.PAID) {
                    applicationsRepository.deleteById(app.getId());
                    log.info("Cell application {} was deleted as NOT PAID on pending", app.getId());
                }
            } catch (InterruptedException e) {
                log.warn("{} was interrupted", this.getClass().getSimpleName());
                Thread.currentThread().interrupt();
            }
        });
    }

    public boolean isPending(final Cell cell) {
        return cellsRepository.isPending(cell);
    }

    @PreDestroy
    @Override
    public void close() {
        log.info("Closing the Vault...");
        pendingKeepersPool.shutdownNow();
        leasingController.stop();
    }
}
