package kspt.bank.boundaries;

import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.PassportInfo;

public interface ClientsRepository {
    boolean containsClientWith(PassportInfo passportInfo);

    Client getClientWith(PassportInfo passportInfo);

    void add(Client client);
}
