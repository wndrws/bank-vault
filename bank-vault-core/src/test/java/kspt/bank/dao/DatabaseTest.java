package kspt.bank.dao;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;

@Disabled
@Deprecated
class DatabaseTest {
    private static Connection conn;

    private Savepoint savepoint;

    @BeforeAll
    static void openConnection()
    throws Exception {
        conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);
    }

    @AfterAll
    static void closeConnection()
    throws Exception {
        DatabaseConnection.closeConnection();
    }

    @BeforeEach
    void setUp()
    throws SQLException {
        savepoint = conn.setSavepoint();
    }

    @Test
    void connectionTest()
    throws SQLException {
        final ResultSet rs = conn.createStatement().executeQuery("SELECT VERSION()");
        if (rs.next()) {
            System.out.println(rs.getString(1));
        }
    }

    @AfterEach
    void tearDown()
    throws SQLException {
        conn.rollback(savepoint);
    }
}
