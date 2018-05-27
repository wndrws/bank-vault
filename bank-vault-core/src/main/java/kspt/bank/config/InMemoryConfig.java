package kspt.bank.config;

import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.dao.InMemoryApplicationsRepository;
import kspt.bank.dao.InMemoryClientsRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "database.enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryConfig {
    @Bean
    public ApplicationsRepository applicationsRepository() {
        return new InMemoryApplicationsRepository();
    }

    @Bean
    public ClientsRepository clientsRepository() {
        return new InMemoryClientsRepository();
    }
}
