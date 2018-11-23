package kspt.bank.services;

import kspt.bank.boundaries.NotificationGate;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.Client;

import java.time.LocalDate;

public class DummyNotificationGate implements NotificationGate {
    @Override
    public void notifyManager(String message) { }

    @Override
    public void notifyManagerAboutLeasingEnd(Cell cell) { }

    @Override
    public void notifyClient(Client client, String message) { }

    @Override
    public void notifyClientAboutArrangement(Client client, String message, LocalDate date) { }

    @Override
    public void notifyClientAboutLeasingExpiration(Client client, Cell cell) { }
}
