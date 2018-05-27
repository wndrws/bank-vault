package kspt.bank.domain.entities;

import kspt.bank.enums.ManipulationType;
import lombok.Getter;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ManipulationLog {
    @Getter
    private final List<Entry> manipulations = new ArrayList<>();

    public void logEvent(final String action, final Client client, final Cell cell) {
        manipulations.add(new Entry(
                LocalDateTime.now(), client, cell, action, ManipulationType.NONE));
    }

    public void logPutManipulation(final String action, final Client client, final Cell cell) {
        manipulations.add(new Entry(
                LocalDateTime.now(), client, cell, action, ManipulationType.PUT));
    }

    public void logGetManipulation(final String action, final Client client, final Cell cell) {
        manipulations.add(new Entry(
                LocalDateTime.now(), client, cell, action, ManipulationType.GET));
    }

    @Value
    private static class Entry {
        LocalDateTime timestamp;

        Client subject;

        Cell object;

        String action;

        ManipulationType manipulationType;
    }
}
