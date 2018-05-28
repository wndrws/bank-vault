package kspt.bank.dao;

import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.domain.entities.CellApplication;
import kspt.bank.domain.entities.Client;

import java.util.Collection;

public class DatabaseApplicationsRepository implements ApplicationsRepository {
    @Override
    public void save(CellApplication application) {
        final CellApplicationDataMapper mapper = (CellApplicationDataMapper)
                DataMapperRegistry.getMapper(CellApplication.class);
        mapper.save(application);
    }

    @Override
    public Collection<CellApplication> getByClient(Client client) {
        final CellApplicationDataMapper mapper = (CellApplicationDataMapper)
                DataMapperRegistry.getMapper(CellApplication.class);
        return mapper.findAllByClient(client);
    }

    @Override
    public CellApplication find(Integer id) {
        final CellApplicationDataMapper mapper = (CellApplicationDataMapper)
                DataMapperRegistry.getMapper(CellApplication.class);
        return mapper.find(id);
    }

    @Override
    public Collection<CellApplication> findAll() {
        final CellApplicationDataMapper mapper = (CellApplicationDataMapper)
                DataMapperRegistry.getMapper(CellApplication.class);
        return mapper.findAll();
    }
}
