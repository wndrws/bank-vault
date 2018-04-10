package kspt.bank.domain;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.Client;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class LeasingController {
    private final static int TIMERS_POOL_SIZE = 1;

    private final Clock clock;

    @Getter @Setter
    private long timersCheckPeriodMillis = 100;

    @Getter
    private final Map<Cell, CellLeaseRecord> leasingInfo = new HashMap<>();

    private final ScheduledExecutorService timersPool =
            Executors.newScheduledThreadPool(TIMERS_POOL_SIZE,
            new ThreadFactoryBuilder().setNameFormat("vault-leasectrl-timersPool-%d").build());

    public void startLeasing(final Cell cell, final Client leaseholder, final Period period) {
        final LocalDate today = LocalDate.now(clock);
        leasingInfo.put(cell, new CellLeaseRecord(leaseholder, today, today.plus(period)));
        timersPool.scheduleAtFixedRate(this::checkLeasingPeriods,
                timersCheckPeriodMillis, timersCheckPeriodMillis, TimeUnit.MILLISECONDS);
    }

    private void checkLeasingPeriods() {
        final LocalDate today = LocalDate.now(clock);
        leasingInfo.values().forEach(leaseRecord -> {
            if (!leaseRecord.expired && leaseRecord.leaseEnd.isBefore(today)) {
                leaseRecord.expired = true;
            }
        });
    }

    public void endLeasing(final Cell cell) {
        leasingInfo.remove(cell);
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

    ImmutableMap<Cell, Client> getCellsAndLeaseholders() {
        return leasingInfo.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(
                        entry.getKey(), entry.getValue().leaseholder))
                .collect(ImmutableMap.toImmutableMap(
                        Map.Entry::getKey, Map.Entry::getValue));
    }

    @RequiredArgsConstructor
    private static class CellLeaseRecord {
        final Client leaseholder;

        final LocalDate leaseBegin;

        final LocalDate leaseEnd;

        boolean expired = false;
    }
}
