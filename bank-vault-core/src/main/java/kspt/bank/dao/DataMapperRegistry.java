package kspt.bank.dao;

import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellApplication;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.Precious;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class DataMapperRegistry {
    private static Map<Class<? extends DomainObject>, AbstractDataMapper> registry = new HashMap<>();

    public static AbstractDataMapper getMapper(Class<? extends DomainObject> clazz) {
        return registry.get(clazz);
    }

    public static void initialize(Connection connection, final boolean useCache) {
        registry.put(Cell.class, new CellDataMapper(connection, useCache));
        registry.put(Precious.class, new PreciousDataMapper(connection, useCache));
        registry.put(Client.class, new ClientDataMapper(connection, useCache));
        registry.put(CellApplication.class, new CellApplicationDataMapper(connection, useCache));
    }

    public static void clear() {
        registry.forEach((clazz, mapper) -> mapper.clearCache());
        registry.clear();
    }
}
