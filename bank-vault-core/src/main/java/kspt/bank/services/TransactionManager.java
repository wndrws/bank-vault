package kspt.bank.services;

import kspt.bank.dao.DataMapperRegistry;
import kspt.bank.dao.DatabaseConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.function.Supplier;

@Slf4j
@Component
class TransactionManager {
    @Value("${database.enabled}")
    private Boolean usingDatabase;

    @Value("${database.autocommit}")
    private Boolean autocommitEnabled;

    @Value("${database.persist}")
    private Boolean persistenceEnabled;

    private Connection connection;

    private Savepoint savepoint;

    @PostConstruct
    private void openConnection()
    throws SQLException {
        if (usingDatabase) {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(autocommitEnabled);
            DataMapperRegistry.initialize(connection, false);
            if (!autocommitEnabled && !persistenceEnabled) {
                savepoint = connection.setSavepoint();
            }
        }
    }

    @PreDestroy
    private void closeConnection()
    throws SQLException {
        if (usingDatabase) {
            if (!autocommitEnabled && !persistenceEnabled) {
                connection.rollback(savepoint);
            }
            DatabaseConnection.closeConnection();
            DataMapperRegistry.clear();
        }
    }

    <T> T runTransactional(Supplier<T> action) {
        if (usingDatabase && !autocommitEnabled) {
            try {
                startTransaction();
                final T result = action.get();
                commitTransaction();
                return result;
            } catch (Throwable t) {
                rollbackTransaction();
                return null;
            }
        } else {
            return action.get();
        }
    }

    void runTransactional(Runnable action) {
        runTransactional(() -> { action.run(); return null; });
    }

    private void startTransaction()
    throws SQLException {
        if (persistenceEnabled) {
            savepoint = connection.setSavepoint();
        }
    }

    private void commitTransaction()
    throws SQLException {
        if (persistenceEnabled) {
            connection.commit();
            savepoint = null;
        }
    }

    private void rollbackTransaction() {
        if (persistenceEnabled) {
            try {
                connection.rollback(savepoint);
            } catch (SQLException e) {
                log.error("Could not rollback transaction!", e);
            }
        }
    }
}
