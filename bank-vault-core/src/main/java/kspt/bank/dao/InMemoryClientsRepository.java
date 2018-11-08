package kspt.bank.dao;

import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.PassportInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryClientsRepository implements ClientsRepository {
    private static AtomicInteger ID_COUNTER = new AtomicInteger(0);

    private final Set<Client> clients = Collections.synchronizedSet(new HashSet<>());

    @Override
    public boolean containsClientWith(PassportInfo passportInfo) {
        return clients.stream()
                .map(Client::getPassportInfo)
                .anyMatch(it -> it.equals(passportInfo));
    }

    @Override
    public Client getClientWith(PassportInfo passportInfo) {
        return clients.stream()
                .filter(client -> client.getPassportInfo().equals(passportInfo))
                .findFirst().orElse(null);
    }

    @Override
    public Client find(Integer id) {
        return clients.stream()
                .filter(client -> client.getId() == id)
                .findFirst().orElse(null);
    }

    @Override
    public void add(Client client) {
        if (client.getId() == null) {
            client.setId(ID_COUNTER.getAndIncrement());
        } else {
            clients.removeIf(c -> c.getId().equals(client.getId()));
        }
        clients.add(client);
    }
}
