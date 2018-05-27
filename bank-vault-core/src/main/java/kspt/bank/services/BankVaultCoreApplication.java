package kspt.bank.services;

import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.boundaries.NotificationGate;
import kspt.bank.dao.InMemoryApplicationsRepository;
import kspt.bank.dao.InMemoryClientsRepository;
import kspt.bank.domain.CellApplicationInteractor;
import kspt.bank.domain.Vault;
import kspt.bank.external.PaymentGate;
import kspt.bank.external.SimplePaymentSystem;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@Slf4j
@EnableAutoConfiguration
@SpringBootApplication
public class BankVaultCoreApplication {
    @Getter
    private static ConfigurableApplicationContext applicationContext;

    private static NotificationGate notificationGate;

    @Bean
    public ApplicationsRepository applicationsRepository() {
        return new InMemoryApplicationsRepository();
    }

    @Bean
    public ClientsRepository clientsRepository() {
        return new InMemoryClientsRepository();
    }

    @Bean
    public PaymentGate paymentGate() {
        return new SimplePaymentSystem();
    }

    @Bean
    public NotificationGate notificationGate() {
        return notificationGate;
    }

    @Bean
    public CellApplicationInteractor cellApplicationInteractor(
            final ApplicationsRepository applicationsRepository,
            final ClientsRepository clientsRepository,
            final PaymentGate paymentGate) {
        return new CellApplicationInteractor(clientsRepository, applicationsRepository, paymentGate);
    }

    public static void start(NotificationGate ng) {
        notificationGate = ng;
        applicationContext = SpringApplication.run(BankVaultCoreApplication.class);
    }

    public static void shutdown() {
        log.warn("System shutdown was requested...");
        Vault.getInstance().stop();
        log.warn("The system is shut down.");
        applicationContext.close();
    }
}
