package kspt.bank.dao;

import com.google.common.base.Preconditions;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellApplication;
import kspt.bank.enums.CellApplicationStatus;
import kspt.bank.domain.entities.Client;

import java.sql.*;
import java.time.Period;
import java.util.List;

public class CellApplicationDataMapper extends AbstractDataMapper {
    private static final String TABLE_NAME = "CellApplication";

    private static final String COLUMNS =
            "client_id, cell_id, period_y, period_m, period_d, status";

    CellApplicationDataMapper(Connection databaseConnection, boolean useCache) {
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
        return "INSERT INTO " + TABLE_NAME + " VALUES (?, ?, ?, ?, ?, ?, ?::cell_app_status)";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE " + TABLE_NAME
                + " SET (" + COLUMNS + ") = (?, ?, ?, ?, ?, ?::cell_app_status) WHERE "
                + AbstractDataMapper.PK_COLUMN_LABEL + "=?";
    }

    @Override
    protected void doInsert(DomainObject object, PreparedStatement insertStatement)
    throws SQLException {
        final CellApplication application = (CellApplication) object;
        insertStatement.setInt(2, application.getLeaseholder().getId());
        setForeignKey(3, application.getCell(), insertStatement);
        if (application.getLeasePeriod() != null) {
            insertStatement.setInt(4, application.getLeasePeriod().getYears());
            insertStatement.setInt(5, application.getLeasePeriod().getMonths());
            insertStatement.setInt(6, application.getLeasePeriod().getDays());
        } else {
            insertStatement.setNull(4, Types.INTEGER);
            insertStatement.setNull(5, Types.INTEGER);
            insertStatement.setNull(6, Types.INTEGER);
        }
        insertStatement.setString(7, application.getStatus().toString());
    }

    @Override
    protected void doUpdate(DomainObject object, PreparedStatement updateStatement)
    throws SQLException {
        final CellApplication application = (CellApplication) object;
        updateStatement.setInt(1, application.getLeaseholder().getId());
        setForeignKey(2, application.getCell(), updateStatement);
        if (application.getLeasePeriod() != null) {
            updateStatement.setInt(3, application.getLeasePeriod().getYears());
            updateStatement.setInt(4, application.getLeasePeriod().getMonths());
            updateStatement.setInt(5, application.getLeasePeriod().getDays());
        } else {
            updateStatement.setNull(3, Types.INTEGER);
            updateStatement.setNull(4, Types.INTEGER);
            updateStatement.setNull(5, Types.INTEGER);
        }
        updateStatement.setString(6, application.getStatus().toString());
        updateStatement.setInt(7, application.getId());
    }

    @Override
    protected DomainObject doLoad(int id, ResultSet rs)
    throws SQLException {
        final ClientDataMapper clientMapper = (ClientDataMapper) DataMapperRegistry.getMapper(Client.class);
        final Client leaseholder = clientMapper.find(rs.getInt("client_id"));
        Preconditions.checkNotNull(leaseholder, "Leaseholder is undefined!");
        final CellDataMapper cellMapper = (CellDataMapper) DataMapperRegistry.getMapper(Cell.class);
        final Cell cell = cellMapper.find(rs.getInt("cell_id"));
        final Period period = Period.of(rs.getInt("period_y"),
                rs.getInt("period_m"), rs.getInt("period_d"));
        final CellApplicationStatus status = CellApplicationStatus.valueOf(rs.getString("status"));
        return new CellApplication(id, leaseholder, cell, period, status);
    }

    CellApplication find(int id) {
        return (CellApplication) findOne(id);
    }

    CellApplication findByCell(Cell cell) {
        return (CellApplication) findOneByCustomWhere("cell_id = ?", cell.getId());
    }

    @SuppressWarnings("unchecked")
    List<CellApplication> findAllByClient(final Client client) {
        return findAllByCustomWhere("client_id = ?", client.getId());
    }

    @SuppressWarnings("unchecked")
    List<CellApplication> findAll() {
        return super.findAll();
    }
}
