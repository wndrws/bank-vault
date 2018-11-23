package kspt.bank.domain;

import com.google.common.collect.*;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import kspt.bank.boundaries.CellsRepository;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellLeaseRecord;
import kspt.bank.domain.entities.Client;
import kspt.bank.enums.CellSize;
import lombok.Getter;
import lombok.Setter;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.AbstractMap;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LeasingController {
    private final static int TIMERS_POOL_SIZE = 1;

    private final Clock clock;

    @Getter @Setter
    private long timersCheckPeriodMillis = 500;

    @Getter
    private final BiMap<Cell, CellLeaseRecord> leasingInfo;

    private final CellsRepository cellsRepository;

    public LeasingController(final Clock clock,final CellsRepository cellsRepository) {
        this.clock = clock;
        this.leasingInfo = HashBiMap.create();
        this.cellsRepository = cellsRepository;
    }

    public LeasingController(final Clock clock, final EnumMap<CellSize, List<Cell>> cells,
            final CellsRepository cellsRepository) {
        this.clock = clock;
        this.leasingInfo = collectLeasingInfo(cells);
        this.cellsRepository = cellsRepository;
    }

    private BiMap<Cell, CellLeaseRecord> collectLeasingInfo(
            EnumMap<CellSize, List<Cell>> cells) {
        return cells.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .map(cell -> new AbstractMap.SimpleEntry<>(cell, cell.getCellLeaseRecord()))
                .filter(entry -> entry.getValue() != null)
                .collect(ImmutableBiMap.toImmutableBiMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private final ScheduledExecutorService timersPool =
            Executors.newScheduledThreadPool(TIMERS_POOL_SIZE,
            new ThreadFactoryBuilder().setNameFormat("vault-leasectrl-timersPool-%d").build());

    public void startLeasing(final Cell cell, final Client leaseholder, final Period period) {
        final LocalDate today = LocalDate.now(clock);
        leasingInfo.put(cell, new CellLeaseRecord(leaseholder, today, today.plus(period), false));
        cell.setCellLeaseRecord(leasingInfo.get(cell));
        cellsRepository.saveCell(cell);
        timersPool.scheduleAtFixedRate(this::checkLeasingPeriods,
                timersCheckPeriodMillis, timersCheckPeriodMillis, TimeUnit.MILLISECONDS);
    }

    private void checkLeasingPeriods() {
        final LocalDate today = LocalDate.now(clock);
        leasingInfo.values().forEach(leaseRecord -> {
            if (!leaseRecord.expired && leaseRecord.leaseEnd.isBefore(today)) {
                leaseRecord.expired = true;
                cellsRepository.saveCell(leasingInfo.inverse().get(leaseRecord));
            }
        });
    }

    public void endLeasing(final Cell cell) {
        leasingInfo.remove(cell);
        cell.setCellLeaseRecord(null);
        cellsRepository.saveCell(cell);
    }

    public boolean isLeased(final Cell cell) {
        return leasingInfo.containsKey(cell);
    }

    public boolean isLeasingExpired(final Cell cell) {
        if (isLeased(cell)) {
            return leasingInfo.get(cell).expired;
        }
        return false;
    }

    public void stop() {
        timersPool.shutdownNow();
    }

    ImmutableMap<Cell, Client> getCellsAndLeaseholders() {
        return leasingInfo.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(
                        entry.getKey(), entry.getValue().leaseholder))
                .collect(ImmutableMap.toImmutableMap(
                        Map.Entry::getKey, Map.Entry::getValue));
    }

    public Range<LocalDate> getInfo(final Cell cell) {
        if (leasingInfo.containsKey(cell)) {
            return Range.closed(leasingInfo.get(cell).leaseBegin, leasingInfo.get(cell).leaseEnd);
        } else {
            return null;
        }
    }
}
