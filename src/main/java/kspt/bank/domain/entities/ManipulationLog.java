package kspt.bank.domain.entities;

import lombok.Getter;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ManipulationLog {
    @Getter
    private final List<Entry> manipulations = new ArrayList<>();

    public void logEvent(final String action, final Client client, final Cell cell) {
        manipulations.add(new Entry(LocalDateTime.now(), client, cell, action));
    }

    @Value
    private static class Entry {
        LocalDateTime timestamp;

        Client subject;

        Cell object;

        String action;
    }
}
