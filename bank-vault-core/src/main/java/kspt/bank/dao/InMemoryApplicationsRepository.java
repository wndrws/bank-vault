package kspt.bank.dao;


import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellApplication;
import kspt.bank.domain.entities.Client;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class InMemoryApplicationsRepository implements ApplicationsRepository {
    private static AtomicInteger ID_COUNTER = new AtomicInteger(0);

    private final Map<Integer, Set<CellApplication>> clientIdToApplications =
            Collections.synchronizedNavigableMap(new TreeMap<>());

    @Override
    public CellApplication save(CellApplication application) {
        final Set<CellApplication> clientsApplications = clientIdToApplications.getOrDefault(
                application.getLeaseholder().getId(), new HashSet<>());
        if (application.getId() == null) {
            application.setId(ID_COUNTER.getAndIncrement());
        } else {
            clientsApplications.remove(application);
        }
        clientsApplications.add(application);
        clientIdToApplications.put(application.getLeaseholder().getId(), clientsApplications);
        return application;
    }

    @Override
    public Collection<CellApplication> findAllByLeaseholder(Client client) {
        return clientIdToApplications.getOrDefault(client.getId(), new HashSet<>());
    }

    @Override
    public CellApplication find(Integer id) {
        return clientIdToApplications.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .filter(app -> app.getId().equals(id))
                .findFirst().orElse(null);
    }

    @Override
    public Collection<CellApplication> findAll() {
        return clientIdToApplications.values().stream()
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Integer id) {
        final Optional<CellApplication> application = clientIdToApplications.values().stream()
                .flatMap(Collection::stream)
                .filter(app -> app.getId().equals(id)).findFirst();
        application.ifPresent(cellApplication -> clientIdToApplications
                .get(cellApplication.getLeaseholder().getId())
                .remove(cellApplication));
    }

    @Override
    public CellApplication findByCell(Cell cell) {
        return clientIdToApplications.values().stream()
                .flatMap(Collection::stream)
                .filter(app -> app.getCell().equals(cell))
                .findFirst().orElse(null);
    }
}
