package kspt.bank.dao;

import kspt.bank.domain.LeasingController;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.Client;
import kspt.bank.enums.CellSize;
import kspt.bank.domain.entities.Precious;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class CellDataMapper extends AbstractDataMapper {
    private static final String TABLE_NAME = "Cell";

    private static final String COLUMNS = "size, precious_id, client_id, lease_begin, lease_end, expired, pending";

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
        return "INSERT INTO " + TABLE_NAME + " VALUES (?, ?::cell_size, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE " + TABLE_NAME +
                " SET (" + COLUMNS + ") = (?::cell_size, ?, ?, ?, ?, ?, ?) WHERE "
                + AbstractDataMapper.PK_COLUMN_LABEL + "=?";
    }

    @Override
    protected void doInsert(final DomainObject object, final PreparedStatement insertStatement)
    throws SQLException {
        final Cell cell = (Cell) object;
        insertStatement.setString(2, cell.getSize().toString());
        setForeignKey(3, cell.getContainedPrecious(), insertStatement);
        final LeasingController.CellLeaseRecord cellLeaseRecord = cell.getCellLeaseRecord();
        if (cellLeaseRecord != null) {
            insertStatement.setInt(4, cellLeaseRecord.leaseholder.getId());
            insertStatement.setDate(5, Date.valueOf(cellLeaseRecord.leaseBegin));
            insertStatement.setDate(6, Date.valueOf(cellLeaseRecord.leaseEnd));
            insertStatement.setBoolean(7, cellLeaseRecord.expired);
        } else {
            insertStatement.setNull(4, Types.INTEGER);
            insertStatement.setNull(5, Types.DATE);
            insertStatement.setNull(6, Types.DATE);
            insertStatement.setNull(7, Types.BOOLEAN);
        }
        insertStatement.setBoolean(8, cell.isPending());
    }

    @Override
    protected void doUpdate(final DomainObject object, final PreparedStatement updateStatement)
    throws SQLException {
        final Cell cell = (Cell) object;
        updateStatement.setString(1, cell.getSize().toString());
        setForeignKey(2, cell.getContainedPrecious(), updateStatement);
        final LeasingController.CellLeaseRecord cellLeaseRecord = cell.getCellLeaseRecord();
        if (cellLeaseRecord != null) {
            updateStatement.setInt(3, cellLeaseRecord.leaseholder.getId());
            updateStatement.setDate(4, Date.valueOf(cellLeaseRecord.leaseBegin));
            updateStatement.setDate(5, Date.valueOf(cellLeaseRecord.leaseEnd));
            updateStatement.setBoolean(6, cellLeaseRecord.expired);
        } else {
            updateStatement.setNull(3, Types.INTEGER);
            updateStatement.setNull(4, Types.DATE);
            updateStatement.setNull(5, Types.DATE);
            updateStatement.setNull(6, Types.BOOLEAN);
        }
        updateStatement.setBoolean(7, cell.isPending());
        updateStatement.setInt(8, cell.getId());
    }

    @Override
    protected DomainObject doLoad(final int id, final ResultSet rs)
    throws SQLException {
        final CellSize size = CellSize.valueOf(rs.getString("size"));
        final Integer containedPreciousId = rs.getInt("precious_id");
        final Integer leaseholderId = rs.getInt("client_id");
        final Boolean pending = rs.getBoolean("pending");
        LeasingController.CellLeaseRecord cellLeaseRecord = null;
        final ClientDataMapper clientMapper = (ClientDataMapper)
                DataMapperRegistry.getMapper(Client.class);
        if (leaseholderId != 0 && clientMapper != null) {
            final Client leaseholder = clientMapper.find(leaseholderId);
            final LocalDate leaseBegin = rs.getDate("lease_begin").toLocalDate();
            final LocalDate leaseEnd = rs.getDate("lease_end").toLocalDate();
            final Boolean expired = rs.getBoolean("expired");
            cellLeaseRecord = new LeasingController.CellLeaseRecord(leaseholder,
                    leaseBegin, leaseEnd, expired);
        }
        final PreciousDataMapper preciousMapper = (PreciousDataMapper)
                DataMapperRegistry.getMapper(Precious.class);
        return preciousMapper == null ? new Cell(id, size, null, cellLeaseRecord, pending) :
                new Cell(id, size, preciousMapper.find(containedPreciousId), cellLeaseRecord, pending);
    }

    public Cell find(final Integer id) {
        return (Cell) findOne(id);
    }

    public boolean isPending(final Cell cell) {
        return findOneByCustomWhere("id = ? AND pending = ?", cell.getId(), Boolean.TRUE) != null;
    }

    @SuppressWarnings("unchecked")
    public List<Cell> findAllPendingCells() {
        return findAllByCustomWhere("pending = ?", Boolean.TRUE);
    }

    @SuppressWarnings("unchecked")
    public List<Cell> findAll() {
        return super.findAll();
    }
}
