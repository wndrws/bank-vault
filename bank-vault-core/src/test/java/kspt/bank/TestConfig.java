package kspt.bank;

import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.CellsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.boundaries.NotificationGate;
import kspt.bank.dao.InMemoryApplicationsRepository;
import kspt.bank.dao.InMemoryCellsRepository;
import kspt.bank.dao.InMemoryClientsRepository;
import kspt.bank.domain.entities.ManipulationLog;
import kspt.bank.external.PaymentSystem;
import kspt.bank.external.SimplePaymentSystem;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {
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

    @Bean
    public PaymentSystem paymentSystem() {
        return new SimplePaymentSystem();
    }

    @MockBean
    private NotificationGate notificationGate;

    @MockBean
    private ManipulationLog manipulationLog;
}
