package kspt.bank;

import kspt.bank.boundaries.NotificationGate;
import kspt.bank.services.DummyNotificationGate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Slf4j
@EnableAutoConfiguration
@EnableJpaRepositories
@SpringBootApplication
public class BankVaultCoreApplication {
    @Getter
    private static ConfigurableApplicationContext applicationContext;

    private static NotificationGate notificationGate = new DummyNotificationGate();

    @Bean
    public NotificationGate notificationGate() {
        return notificationGate;
    }

    public static void start(NotificationGate ng, WebApplicationType type, String[] argv) {
        notificationGate = ng;
        applicationContext =
                new SpringApplicationBuilder(BankVaultCoreApplication.class).web(type).run(argv);
    }

    public static void shutdown() {
        log.warn("System shutdown was requested...");
        applicationContext.close();
    }
}
