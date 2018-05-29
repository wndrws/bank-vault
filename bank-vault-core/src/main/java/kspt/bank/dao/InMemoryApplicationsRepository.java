package kspt.bank.dao;

import com.google.common.base.Preconditions;
import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellApplication;
import kspt.bank.domain.entities.Client;
import kspt.bank.enums.CellApplicationStatus;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryApplicationsRepository implements ApplicationsRepository {
    private final Map<Integer, Set<CellApplication>> repository = new TreeMap<>();

    @Override
    public void save(CellApplication application) {
        final Set<CellApplication> clientsApplications =
                repository.getOrDefault(application.getLeaseholder().getId(), new HashSet<>());
        Preconditions.checkState(!clientsApplications.contains(application));
        clientsApplications.add(application);
        repository.put(application.getLeaseholder().getId(), clientsApplications);
    }

    @Override
    public Collection<CellApplication> getByClient(Client client) {
        return repository.getOrDefault(client.getId(), new HashSet<>());
    }

    @Override
    public CellApplication find(Integer id) {
        return repository.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .filter(app -> app.getId().equals(id))
                .findFirst().orElse(null);
    }

    @Override
    public Collection<CellApplication> findAll() {
        return repository.values().stream()
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public void deleteApplicationForCell(Cell cell) {
        final Optional<CellApplication> application = repository.values().stream()
                .flatMap(Collection::stream)
                .filter(app -> app.getCell().equals(cell)).findFirst();
        if (application.isPresent()) {
            repository.get(application.get().getLeaseholder().getId()).remove(application.get());
        }
    }

    @Override
    public void deleteApplication(Integer id) {
        final Optional<CellApplication> application = repository.values().stream()
                .flatMap(Collection::stream)
                .filter(app -> app.getId().equals(id)).findFirst();
        if (application.isPresent()) {
            repository.get(application.get().getLeaseholder().getId()).remove(application.get());
        }
    }
}
