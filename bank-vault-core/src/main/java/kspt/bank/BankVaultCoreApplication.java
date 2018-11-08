package kspt.bank;

import kspt.bank.boundaries.NotificationGate;
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
    public NotificationGate notificationGate() {
        return notificationGate;
    }

    public static void start(NotificationGate ng, String[] argv) {
        notificationGate = ng;
        applicationContext = SpringApplication.run(BankVaultCoreApplication.class, argv);
    }

    public static void shutdown() {
        log.warn("System shutdown was requested...");
        applicationContext.close();
    }
}
