package kspt.bank.dao;

import lombok.RequiredArgsConstructor;

import java.sql.*;
import java.util.*;

@RequiredArgsConstructor
abstract class AbstractDataMapper {
    static final String PK_COLUMN_LABEL = "id";

    private final Connection databaseConnection;

    private final boolean useLoadedObjectsCache;

    private Map<Integer, DomainObject> loadedObjects = new HashMap<>();

    void clearCache() {
        loadedObjects.clear();
    }

    DomainObject findOne(final Integer id) {
        if (useLoadedObjectsCache && loadedObjects.containsKey(id)) {
            return loadedObjects.get(id);
        } else try {
            return tryToFindOne(databaseConnection, id);
        } catch (SQLException e) {
            DatabaseConnection.closeConnection();
            throw new RuntimeException(e);
        }
    }

    private DomainObject tryToFindOne(final Connection conn, final Integer id)
    throws SQLException {
        final PreparedStatement findStatement = conn.prepareStatement(getReadQuery());
        findStatement.setInt(1, id);
        ResultSet rs = findStatement.executeQuery();
        DomainObject object = load(rs);
        rs.close();
        findStatement.close();
        return object;
    }

    private DomainObject load(final ResultSet rs)
    throws SQLException {
        if (rs.next()) {
            final int id = rs.getInt(PK_COLUMN_LABEL);
            final DomainObject result = doLoad(id, rs);
            if (useLoadedObjectsCache) {
                loadedObjects.put(id, result);
            }
            return result;
        } else {
            return null;
        }
    }

    void insert(final DomainObject object) {
        try {
            tryToInsert(databaseConnection, object);
        } catch (SQLException e) {
            DatabaseConnection.closeConnection();
            throw new RuntimeException(e);
        }
    }

    private void tryToInsert(final Connection conn, final DomainObject object)
    throws SQLException {
        final PreparedStatement insertStatement = conn.prepareStatement(getCreateQuery());
        insertStatement.setInt(1, object.getId());
        doInsert(object, insertStatement);
        insertStatement.execute();
        insertStatement.close();
        if (useLoadedObjectsCache) {
            loadedObjects.put(object.getId(), object);
        }
    }

    public void save(final DomainObject object) {
        final DomainObject existingObject = findOne(object.getId());
        if (existingObject == null) {
            insert(object);
        } else {
            update(object);
        }
    }

    DomainObject update(final DomainObject object) {
        try {
            return tryToUpdate(databaseConnection, object);
        } catch (SQLException e) {
            DatabaseConnection.closeConnection();
            throw new RuntimeException(e);
        }
    }

    private DomainObject tryToUpdate(Connection conn, DomainObject object)
    throws SQLException {
        final PreparedStatement updateStatement = conn.prepareStatement(getUpdateQuery());
        doUpdate(object, updateStatement);
        updateStatement.execute();
        updateStatement.close();
        if (useLoadedObjectsCache) {
            loadedObjects.put(object.getId(), object);
        }
        return findOne(object.getId());
    }

    void delete(final DomainObject object) {
        try {
            tryToDelete(databaseConnection, object);
        } catch (SQLException e) {
            DatabaseConnection.closeConnection();
            throw new RuntimeException(e);
        }
    }

    private void tryToDelete(Connection conn, DomainObject object)
    throws SQLException {
        final PreparedStatement deleteStatement = conn.prepareStatement(getDeleteQuery());
        deleteStatement.setInt(1, object.getId());
        deleteStatement.execute();
        deleteStatement.close();
        if (useLoadedObjectsCache) {
            loadedObjects.remove(object.getId());
        }
    }

    void setForeignKey(int index, DomainObject foreignObject, final PreparedStatement st)
    throws SQLException {
        if (foreignObject != null ) {
            final AbstractDataMapper mapper = DataMapperRegistry.getMapper(foreignObject.getClass());
            final DomainObject existingForeignObject = mapper.findOne(foreignObject.getId());
            if (existingForeignObject == null) {
                mapper.insert(foreignObject);
            }
            st.setInt(index, foreignObject.getId());
        } else {
            st.setNull(index, Types.INTEGER);
        }
    }

    DomainObject findOneByCustomWhere(String whereClause, Object... args) {
        try {
            return tryToFindOneByCustomWhere(databaseConnection, whereClause, args);
        } catch (SQLException e) {
            DatabaseConnection.closeConnection();
            throw new RuntimeException(e);
        }
    }

    private DomainObject tryToFindOneByCustomWhere(Connection conn, String whereClause, Object... args)
    throws SQLException {
        final String query =
                "SELECT " + PK_COLUMN_LABEL + " FROM " + getTableName() + " WHERE " + whereClause;
        final PreparedStatement findStatement = prepareCustomStatement(conn, query, args);
        final ResultSet rs = findStatement.executeQuery();
        if (rs.next()) {
            return findOne(rs.getInt(1));
        } else {
            return null;
        }
    }

    private static PreparedStatement prepareCustomStatement(Connection conn, String query, Object... args)
    throws SQLException {
        final PreparedStatement findStatement = conn.prepareStatement(query);
        for (int i = 1; i <= args.length; i++) {
            findStatement.setObject(i, args[i - 1]);
        }
        return findStatement;
    }

    List findAllByCustomWhere(String whereClause, Object... args) {
        try {
            return tryToFindAllByCustomWhere(databaseConnection, whereClause, args);
        } catch (SQLException e) {
            DatabaseConnection.closeConnection();
            throw new RuntimeException(e);
        }
    }

    private List tryToFindAllByCustomWhere(Connection conn, String whereClause, Object... args)
    throws SQLException {
        final String query =
                "SELECT " + PK_COLUMN_LABEL + " FROM " + getTableName() + " WHERE " + whereClause;
        final PreparedStatement findStatement = prepareCustomStatement(conn, query, args);
        final ResultSet rs = findStatement.executeQuery();
        final List<DomainObject> results = new ArrayList<>();
        while (rs.next()) {
            results.add(findOne(rs.getInt(1)));
        }
        return results;
    }

    protected abstract void doInsert(DomainObject object, PreparedStatement st) throws SQLException;

    protected abstract void doUpdate(DomainObject object, PreparedStatement st) throws SQLException;

    protected abstract DomainObject doLoad(int id, ResultSet rs) throws SQLException;

    protected abstract String getCreateQuery();

    protected String getReadQuery() {
        return "SELECT " + PK_COLUMN_LABEL + ", " + getColumnNames() + " FROM "
                + getTableName() + " WHERE " + PK_COLUMN_LABEL + "=?";
    }

    protected abstract String getUpdateQuery();

    protected String getDeleteQuery() {
        return "DELETE FROM " + getTableName() + " WHERE " + PK_COLUMN_LABEL + "=?";
    }

    protected abstract String getTableName();

    protected abstract String getColumnNames();
}
