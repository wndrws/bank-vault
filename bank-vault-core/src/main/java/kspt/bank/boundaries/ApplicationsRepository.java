package kspt.bank.boundaries;

import kspt.bank.domain.entities.CellApplication;
import kspt.bank.domain.entities.Client;

import java.util.Collection;

public interface ApplicationsRepository {
    CellApplication save(CellApplication application);

    Collection<CellApplication> findAllByLeaseholder(Client client);

    CellApplication find(Integer id);

    Collection<CellApplication> findAll();

    void deleteById(Integer id);
}
