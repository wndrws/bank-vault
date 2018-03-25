package kspt.bank.boundaries;

import kspt.bank.domain.entities.CellApplication;
import kspt.bank.domain.entities.Client;

import java.util.Collection;

public interface ApplicationsRepository {
    void add(CellApplication application);

    Collection<CellApplication> getByClient(Client client);

    Collection<CellApplication> getAll();
}
