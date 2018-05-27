package kspt.bank.dao;

import kspt.bank.domain.entities.Cell;
import kspt.bank.enums.CellSize;
import kspt.bank.domain.entities.Precious;

import java.sql.*;

public class CellDataMapper extends AbstractDataMapper {
    private static final String TABLE_NAME = "Cell";

    private static final String COLUMNS = "size, precious_id";

    CellDataMapper(final Connection databaseConnection, final boolean useCache) {
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
        return "INSERT INTO " + TABLE_NAME + " VALUES (?, ?::cell_size, ?)";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE " + TABLE_NAME + " SET (" + COLUMNS + ") = (?::cell_size, ?) WHERE "
                + AbstractDataMapper.PK_COLUMN_LABEL + "=?";
    }

    @Override
    protected void doInsert(final DomainObject object, final PreparedStatement insertStatement)
    throws SQLException {
        final Cell cell = (Cell) object;
        insertStatement.setString(2, cell.getSize().toString());
        setForeignKey(3, cell.getContainedPrecious(), insertStatement);
    }

    @Override
    protected void doUpdate(final DomainObject object, final PreparedStatement updateStatement)
    throws SQLException {
        final Cell cell = (Cell) object;
        updateStatement.setString(1, cell.getSize().toString());
        setForeignKey(2, cell.getContainedPrecious(), updateStatement);
        updateStatement.setInt(3, cell.getId());
    }

    @Override
    protected DomainObject doLoad(final int id, final ResultSet rs)
    throws SQLException {
        final CellSize size = CellSize.valueOf(rs.getString("size"));
        final Integer containedPreciousId = rs.getInt("precious_id");
        final PreciousDataMapper preciousMapper = (PreciousDataMapper)
                DataMapperRegistry.getMapper(Precious.class);
        return preciousMapper == null ? new Cell(id, size) :
                new Cell(id, size, preciousMapper.find(containedPreciousId));
    }

    public Cell find(final Integer id) {
        return (Cell) findOne(id);
    }
}
