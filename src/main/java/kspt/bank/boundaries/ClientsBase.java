package kspt.bank.boundaries;

import kspt.bank.domain.entities.PassportInfo;

public interface ClientsBase {
    boolean containsClientWith(PassportInfo passportInfo);

    void addClientWith(PassportInfo passportInfo);
}
