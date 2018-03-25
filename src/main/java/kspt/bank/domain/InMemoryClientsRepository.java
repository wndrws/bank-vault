package kspt.bank.domain;

import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.PassportInfo;

import java.util.HashSet;
import java.util.Set;

public class InMemoryClientsRepository implements ClientsRepository {
    private final Set<Client> clients = new HashSet<>();

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
    public void add(Client client) {
        clients.add(client);
    }
}
