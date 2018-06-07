package kspt.bank.config;

import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.boundaries.NotificationGate;
import kspt.bank.domain.CellApplicationInteractor;
import kspt.bank.domain.CellManipulationInteractor;
import kspt.bank.domain.entities.ManipulationLog;
import kspt.bank.external.FileBasedPaymentSystem;
import kspt.bank.external.PaymentSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {
    @Bean
    public PaymentSystem paymentSystem() {
        return new FileBasedPaymentSystem();
    }

    @Bean
    public ManipulationLog manipulationLog() {
        return new ManipulationLog();
    }

    @Bean
    public CellApplicationInteractor cellApplicationInteractor(
            final ApplicationsRepository applicationsRepository,
            final ClientsRepository clientsRepository,
            final PaymentSystem paymentSystem) {
        return new CellApplicationInteractor(clientsRepository, applicationsRepository, paymentSystem);
    }

    @Bean
    public CellManipulationInteractor cellManipulationInteractor(
            final ManipulationLog manipulationLog, final NotificationGate notificationGate) {
        return new CellManipulationInteractor(manipulationLog, notificationGate);
    }
}
