package kspt.bank.dao;

import lombok.AccessLevel;
import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "5432";
    private static final String DB_NAME = "bank_vault";
    private static final String DB_USER = "jdbc";
    private static final String DB_PASS = "JavaJ";
    private static final String DB_URL =
            "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;

    private static Connection connection;

    public static synchronized Connection getConnection() {
        if (connection == null) {
            connection = createConnection();
        }
        return connection;
    }

    private static Connection createConnection() {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void closeConnection() {
        try {
            getConnection().close();
            connection = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
