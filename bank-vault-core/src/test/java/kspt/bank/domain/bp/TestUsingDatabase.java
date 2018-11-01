package kspt.bank.domain.bp;

import kspt.bank.dao.DatabaseConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

class TestUsingDatabase {

    private static Connection conn;

    private Savepoint savepoint;

    @BeforeAll
    static void openConnection()
    throws SQLException {
        conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);
    }

    @BeforeEach
    void startTransaction()
    throws Exception {
        savepoint = conn.setSavepoint();
    }

    @AfterEach
    void rollbackTransaction()
    throws SQLException {
        conn.rollback(savepoint);
    }

    @AfterAll
    static void closeConnection() {
        DatabaseConnection.closeConnection();
    }
}
