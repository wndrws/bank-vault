package kspt.bank.domain;

import com.google.common.base.Preconditions;
import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.domain.entities.CellApplication;
import kspt.bank.domain.entities.CellApplicationStatus;
import kspt.bank.domain.entities.Client;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryApplicationsRepository implements ApplicationsRepository {
    private final Map<Integer, Set<CellApplication>> repository = new TreeMap<>();

    @Override
    public void add(CellApplication application) {
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
    public Collection<CellApplication> getAll() {
        return repository.values().stream()
                .flatMap(Collection::stream).collect(Collectors.toList());
    }
}
