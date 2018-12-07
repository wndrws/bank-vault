package kspt.bank.boundaries;

import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.PassportInfo;

public interface ClientsRepository {
    boolean containsClientWith(PassportInfo passportInfo);

    Client getClientWith(PassportInfo passportInfo);

    Client find(Integer id);

    void add(Client client);

    boolean containsClientWithSerial(String serial);
}
