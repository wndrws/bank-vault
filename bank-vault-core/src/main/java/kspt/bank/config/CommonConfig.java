package kspt.bank.config;

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
}
