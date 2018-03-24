package kspt.bank.boundaries;

import kspt.bank.domain.entities.PassportInfo;

public interface ClientsRepository {
    boolean containsClientWith(PassportInfo passportInfo);

    void addClientWith(PassportInfo passportInfo);
}
