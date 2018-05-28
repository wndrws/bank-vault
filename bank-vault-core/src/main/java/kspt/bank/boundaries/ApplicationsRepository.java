package kspt.bank.boundaries;

import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellApplication;
import kspt.bank.domain.entities.Client;

import java.util.Collection;

public interface ApplicationsRepository {
    void save(CellApplication application);

    Collection<CellApplication> getByClient(Client client);

    CellApplication find(Integer id);

    Collection<CellApplication> findAll();

    void deleteApplicationForCell(Cell cell);
}
