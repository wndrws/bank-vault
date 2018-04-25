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

    @Getter(value = AccessLevel.PUBLIC, lazy = true)
    private final static Connection connection = createConnection();

    private static Connection createConnection() {
        final String url = "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        try {
            return DriverManager.getConnection(url, DB_USER, DB_PASS);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void closeConnection() {
        try {
            getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
