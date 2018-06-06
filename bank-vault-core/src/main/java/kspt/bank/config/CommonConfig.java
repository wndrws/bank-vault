package kspt.bank.config;

import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.domain.CellApplicationInteractor;
import kspt.bank.external.FileBasedPaymentSystem;
import kspt.bank.external.PaymentSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {
    @Bean
    public PaymentSystem paymentGate() {
        return new FileBasedPaymentSystem();
    }

    @Bean
    public CellApplicationInteractor cellApplicationInteractor(
            final ApplicationsRepository applicationsRepository,
            final ClientsRepository clientsRepository,
            final PaymentSystem paymentSystem) {
        return new CellApplicationInteractor(clientsRepository, applicationsRepository, paymentSystem);
    }
}
