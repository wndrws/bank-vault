package kspt.bank.config;

import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.dao.DatabaseApplicationsRepository;
import kspt.bank.dao.DatabaseClientsRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "database.enabled", havingValue = "true")
public class DatabaseConfig {
    @Bean
    public ApplicationsRepository applicationsRepository() {
        return new DatabaseApplicationsRepository();
    }

    @Bean
    public ClientsRepository clientsRepository() {
        return new DatabaseClientsRepository();
    }
}
