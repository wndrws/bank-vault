package kspt.bank;

import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.CellsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.dao.InMemoryApplicationsRepository;
import kspt.bank.dao.InMemoryCellsRepository;
import kspt.bank.dao.InMemoryClientsRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InMemoryConfig {
    @Bean
    public ApplicationsRepository applicationsRepository() {
        return new InMemoryApplicationsRepository();
    }

    @Bean
    public ClientsRepository clientsRepository() {
        return new InMemoryClientsRepository();
    }

    @Bean
    public CellsRepository cellsRepository() {
        return new InMemoryCellsRepository();
    }
}
