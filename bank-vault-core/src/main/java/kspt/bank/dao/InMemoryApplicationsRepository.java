package kspt.bank.dao;


import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.domain.entities.CellApplication;
import kspt.bank.domain.entities.Client;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryApplicationsRepository implements ApplicationsRepository {
    private final Map<Integer, Set<CellApplication>> repository = new TreeMap<>();

    @Override
    public CellApplication save(CellApplication application) {
        final Set<CellApplication> clientsApplications =
                repository.getOrDefault(application.getLeaseholder().getId(), new HashSet<>());
        if(!clientsApplications.add(application)) {
            clientsApplications.remove(application);
            clientsApplications.add(application);
        }
        repository.put(application.getLeaseholder().getId(), clientsApplications);
        return application;
    }

    @Override
    public Collection<CellApplication> findAllByClient(Client client) {
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
    public void deleteApplication(Integer id) {
        final Optional<CellApplication> application = repository.values().stream()
                .flatMap(Collection::stream)
                .filter(app -> app.getId().equals(id)).findFirst();
        if (application.isPresent()) {
            repository.get(application.get().getLeaseholder().getId()).remove(application.get());
        }
    }
}
