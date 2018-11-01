package kspt.bank.boundaries;

import kspt.bank.domain.entities.CellApplication;
import kspt.bank.domain.entities.Client;

import java.util.Collection;

public interface ApplicationsRepository {
    CellApplication save(CellApplication application);

    Collection<CellApplication> findAllByClient(Client client);

    CellApplication find(Integer id);

    Collection<CellApplication> findAll();

    void deleteApplication(Integer id);
}
