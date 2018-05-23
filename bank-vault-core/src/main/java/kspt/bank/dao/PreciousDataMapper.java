package kspt.bank.dao;

import kspt.bank.domain.entities.Precious;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PreciousDataMapper extends AbstractDataMapper {
    private static final String TABLE_NAME = "Precious";

    private static final String COLUMNS = "name, volume";

    PreciousDataMapper(final Connection databaseConnection, final boolean useCache) {
        super(databaseConnection, useCache);
    }


    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getColumnNames() {
        return COLUMNS;
    }

    @Override
    protected String getCreateQuery() {
        return "INSERT INTO " + TABLE_NAME + " VALUES (?, ?, ?)";
    }

    @Override
    protected String getUpdateQuery() {
        throw new UnsupportedOperationException("Precious objects are immutable");
    }

    @Override
    protected void doInsert(DomainObject object, PreparedStatement st)
    throws SQLException {
        Precious precious = (Precious) object;
        st.setString(2, precious.getName());
        st.setInt(3, precious.getVolume());
    }

    @Override
    protected void doUpdate(DomainObject object, PreparedStatement st)
    throws SQLException {
        throw new UnsupportedOperationException("Precious objects are immutable");
    }

    @Override
    protected DomainObject doLoad(int id, ResultSet rs)
    throws SQLException {
        return new Precious(id, rs.getInt("volume"), rs.getString("name"));
    }

    public Precious find(Integer id) {
        return (Precious) findOne(id);
    }
}
