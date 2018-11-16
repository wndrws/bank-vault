package kspt.bank;

import com.statemachinesystems.mockclock.MockClock;
import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.CellsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.boundaries.NotificationGate;
import kspt.bank.config.VaultConfig;
import kspt.bank.domain.*;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.Precious;
import kspt.bank.enums.CellSize;
import kspt.bank.enums.PaymentMethod;
import kspt.bank.external.Invoice;
import kspt.bank.external.PaymentSystem;
import kspt.bank.external.SimplePaymentSystem;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;


@DataJpaTest
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@AutoConfigurationPackage
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {
        Vault.class, VaultConfig.class, LeasingControlInteractor.class,
        LeasingExpiryTest.Config.class })
class LeasingExpiryTest {
    @Autowired
    private LeasingController leasingController;

    @Autowired
    private ClientsRepository clientsRepository;

    @Autowired
    private ApplicationsRepository applicationsRepository;

    @Autowired
    private CellsRepository cellsRepository;

    @Autowired
    private PaymentSystem paymentSystem;

    @Autowired
    private NotificationGate notificationGate;

    @Autowired
    private MockClock mockedClock;

    @Autowired
    private LeasingControlInteractor lcInteractor;

    private final RoleClient roleClient = new RoleClient();

    private final RoleManager roleManager = new RoleManager();

    private Invoice invoice;

    @Test
    void testBusinessProcess_ContinueLeasing()
    throws InterruptedException {
        assumeThatCellIsLeased();

        waitForLeasingExpiry();
        assertThatLeasingExpired();
        verifyThatClientIsNotifiedAboutExpiry();

        invoice = roleClient.continueLeasing();
        assertFalse(invoice.isPaid());

        roleClient.pay(invoice);
        assertContinuationOfLeasing();
    }

    @Test
    void testBusinessProcess_StopLeasingWhenPreciousIsInCell()
    throws InterruptedException {
        assumeThatCellContainsPrecious();
        assumeThatCellIsLeased();

        waitForLeasingExpiry();
        assertThatLeasingExpired();
        verifyThatClientIsNotifiedAboutExpiry();

        roleClient.stopLeasing();
        verifyThatManagerIsNotified();

        final LocalDate day = roleManager.arrangePreciousExtractionDay();
        verifyThatClientIsNotifiedAboutArrangement(day);
    }

    @Test
    void testBusinessProcess_StopLeasingWhenCellIsEmpty()
    throws InterruptedException {
        assumeThatCellIsEmpty();
        assumeThatCellIsLeased();

        waitForLeasingExpiry();
        assertThatLeasingExpired();
        verifyThatClientIsNotifiedAboutExpiry();

        roleClient.stopLeasing();
        assertThatLeasingIsStopped();
    }

    private void assumeThatCellIsLeased() {
        leasingController.startLeasing(roleClient.cell, roleClient.client,
                roleClient.initialLeasingPeriod);
        Assumptions.assumeTrue(leasingController.isLeased(roleClient.cell));
    }

    private void waitForLeasingExpiry()
    throws InterruptedException {
        mockedClock.advanceByDays(roleClient.initialLeasingPeriod.getDays() + 1);
        Thread.sleep(2 * leasingController.getTimersCheckPeriodMillis());
    }

    private void assertThatLeasingExpired() {
        assertTrue(leasingController.isLeasingExpired(roleClient.cell));
    }

    private void verifyThatClientIsNotifiedAboutExpiry() {
        lcInteractor.sendNotifications();
        verify(notificationGate).notifyClientAboutLeasingExpiration(
                roleClient.client, roleClient.cell);
    }

    private void assertContinuationOfLeasing() {
        assertTrue(invoice.isPaid());
        assertTrue(leasingController.isLeased(roleClient.cell));
        assertFalse(leasingController.isLeasingExpired(roleClient.cell));
    }

    private void verifyThatManagerIsNotified() {
        verify(notificationGate).notifyManagerAboutLeasingEnd(roleClient.cell);
    }

    private void assumeThatCellContainsPrecious() {
        Assumptions.assumeTrue(roleClient.cell != null);
        roleClient.cell.setContainedPrecious(roleClient.precious);
    }

    private void verifyThatClientIsNotifiedAboutArrangement(final LocalDate day) {
        verify(notificationGate).notifyClientAboutArrangement(
                eq(roleClient.client), anyString(), eq(day));
    }

    private void assumeThatCellIsEmpty() {
        Assumptions.assumeTrue(roleClient.cell != null);
        Assumptions.assumeTrue(roleClient.cell.isEmpty());
    }

    private void assertThatLeasingIsStopped() {
        assertFalse(leasingController.isLeased(roleClient.cell));
        verify(notificationGate).notifyClient(eq(roleClient.client), anyString());
    }

    private class RoleClient {
        final Client client = TestDataGenerator.getSampleClient();

        final Precious precious = new Precious(1, "The Ring Of Power");

        final Cell cell = new Cell(CellSize.SMALL);

        final Period initialLeasingPeriod = Period.ofDays(10);

        final Period newLeasingPeriod = Period.ofDays(60);

        Invoice continueLeasing() {
            return lcInteractor.continueLeasing(client, cell, newLeasingPeriod);
        }

        void pay(Invoice invoice) {
            paymentSystem.pay(invoice, invoice.getSum(), PaymentMethod.CASH);
            lcInteractor.acceptPayment(invoice);
        }

        void stopLeasing() {
            lcInteractor.stopLeasing(client, cell);
        }
    }

    private class RoleManager {
        LocalDate arrangePreciousExtractionDay() {
            final LocalDate day = LocalDate.now(mockedClock);
            lcInteractor.arrangeLeasingEnd(roleClient.client, day);
            return day;
        }
    }

    @BeforeEach
    void setUp() {
        cellsRepository.saveCell(roleClient.cell);
        leasingController.setTimersCheckPeriodMillis(
                LeasingControlInteractorTest.LEASING_TIMERS_CHECK_PERIOD_MS);
        clientsRepository.add(roleClient.client);
    }


    @Configuration
    static class Config {
        @Bean
        @Primary
        PaymentSystem paymentSystem() {
            return new SimplePaymentSystem();
        }

        @MockBean
        NotificationGate notificationGate;

        @Bean
        @Primary
        Clock clock() {
            return MockClock.at(2018, 4, 10, 19, 0, ZoneId.systemDefault());
        }

        @Bean
        MockClock mockedClock() {
            return (MockClock) clock();
        }
    }
}
