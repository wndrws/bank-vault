package kspt.bank.boundaries;

import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.Client;

import java.time.LocalDate;

public interface NotificationGate {
    void notifyManager(String message);

    void notifyManagerAboutLeasingEnd(Cell cell);

    void notifyClient(Client client, String message);

    void notifyClientAboutArrangement(Client client, String message, LocalDate date);

    void notifyClientAboutLeasingExpiration(Client client, Cell cell);
}
