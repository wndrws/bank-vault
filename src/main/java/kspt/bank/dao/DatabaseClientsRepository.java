package kspt.bank.dao;

import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.PassportInfo;

public class DatabaseClientsRepository implements ClientsRepository {
    @Override
    public boolean containsClientWith(PassportInfo passportInfo) {
        return getClientWith(passportInfo) != null;
    }

    @Override
    public Client getClientWith(PassportInfo passportInfo) {
        final ClientDataMapper mapper = (ClientDataMapper) DataMapperRegistry.getMapper(Client.class);
        return mapper.findByPassportInfo(passportInfo);
    }

    @Override
    public void add(Client client) {
        final ClientDataMapper mapper = (ClientDataMapper) DataMapperRegistry.getMapper(Client.class);
        mapper.insert(client);
    }
}
