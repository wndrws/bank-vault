package kspt.bank.dao;

import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.PassportInfo;

import java.sql.*;

public class ClientDataMapper extends AbstractDataMapper {
    private static final String TABLE_NAME = "Client";

    private static final String COLUMNS =
            "serial, first_name, last_name, patronymic, birth_date, phone, email";

    ClientDataMapper(Connection databaseConnection, boolean useCache) {
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
        return "INSERT INTO " + TABLE_NAME + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateQuery() {
        throw new UnsupportedOperationException("Client objects are immutable");
    }

    @Override
    protected void doInsert(DomainObject object, PreparedStatement st)
    throws SQLException {
        final Client client = (Client) object;
        st.setString(2, client.getPassportInfo().getSerial());
        st.setString(3, client.getPassportInfo().getFirstName());
        st.setString(4, client.getPassportInfo().getLastName());
        st.setString(5, client.getPassportInfo().getPatronymic());
        st.setDate(6, Date.valueOf(client.getPassportInfo().getBirthDate()));
        st.setString(7, client.getPhone());
        st.setString(8, client.getEmail());
    }

    @Override
    protected void doUpdate(DomainObject object, PreparedStatement st)
    throws SQLException {
        throw new UnsupportedOperationException("Client objects are immutable");
    }

    @Override
    protected DomainObject doLoad(int id, ResultSet rs)
    throws SQLException {
        final PassportInfo passportInfo = new PassportInfo(
                rs.getString("serial"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("patronymic"),
                rs.getDate("birth_date").toLocalDate()
        );
        return new Client(id, passportInfo,
                rs.getString("phone"), rs.getString("email"));
    }

    Client find(Integer id) {
        return (Client) findOne(id);
    }

    Client findByPassportInfo(final PassportInfo info) {
        return (Client) findOneByCustomWhere("serial=? AND first_name=? AND last_name=?",
                info.getSerial(), info.getFirstName(), info.getLastName());
    }

}
