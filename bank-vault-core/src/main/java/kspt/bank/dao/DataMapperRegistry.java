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

    private static Map<Class<? extends AutoIdDomainObject>, Integer> keyTable = new HashMap<>();

    public static AbstractDataMapper getMapper(Class<? extends DomainObject> clazz) {
        return registry.get(clazz);
    }

    synchronized static Integer getCurrentMaxId(Class<? extends AutoIdDomainObject> clazz) {
        final Integer id = keyTable.getOrDefault(clazz, 0);
        keyTable.put(clazz, id + 1);
        return id;
    }

    public synchronized static void initialize(Connection connection, final boolean useCache) {
        registry.put(Cell.class, new CellDataMapper(connection, useCache));
        registry.put(Precious.class, new PreciousDataMapper(connection, useCache));
        registry.put(Client.class, new ClientDataMapper(connection, useCache));
        registry.put(CellApplication.class, new CellApplicationDataMapper(connection, useCache));
        initializeKeyTable();
    }

    private static void initializeKeyTable() {
        keyTable.put(Precious.class, registry.get(Precious.class).selectMaxId());
        keyTable.put(Client.class, registry.get(Client.class).selectMaxId());
        keyTable.put(CellApplication.class, registry.get(CellApplication.class).selectMaxId());
    }

    public synchronized static void clear() {
        registry.forEach((clazz, mapper) -> mapper.clearCache());
        registry.clear();
        keyTable.clear();
    }
}
