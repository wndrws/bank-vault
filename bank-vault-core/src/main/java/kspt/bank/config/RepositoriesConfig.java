package kspt.bank.config;

import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.CellsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.dao.JpaApplicationsRepository;
import kspt.bank.dao.JpaCellsRepository;
import kspt.bank.dao.JpaClientsRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@ConditionalOnProperty(name = "database.enabled", havingValue = "true")
public class RepositoriesConfig {
    @Bean
    CellsRepository cellsRepository(JpaCellsRepository jpaCellsRepository) {
        return jpaCellsRepository;
    }

    @Bean
    ClientsRepository clientsRepository(JpaClientsRepository jpaClientsRepository) {
        return jpaClientsRepository;
    }

    @Bean
    ApplicationsRepository clientsRepository(JpaApplicationsRepository jpaApplicationsRepository) {
        return jpaApplicationsRepository;
    }
}
