package kspt.bank.domain;

import com.statemachinesystems.mockclock.MockClock;
import kspt.bank.TestConfig;
import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.CellsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.boundaries.NotificationGate;
import kspt.bank.config.VaultConfig;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellApplication;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.Precious;
import kspt.bank.enums.CellApplicationStatus;
import kspt.bank.enums.CellSize;
import kspt.bank.enums.PaymentMethod;
import kspt.bank.external.Invoice;
import kspt.bank.external.PaymentSystem;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {
        LeasingControlInteractor.class, Vault.class, VaultConfig.class, TestConfig.class,
        LeasingControlInteractorTest.Config.class})
public class LeasingControlInteractorTest {
    public final static long LEASING_TIMERS_CHECK_PERIOD_MS = 100;

    private final static MockClock MOCKED_CLOCK =
            MockClock.at(2018, 4, 10, 19, 0, ZoneId.systemDefault());

    @Autowired
    private Vault vault;

    @Autowired
    private ApplicationsRepository applicationsRepository;

    @Autowired
    private CellsRepository cellsRepository;

    @Autowired
    private ClientsRepository clientsRepository;

    @Autowired
    private PaymentSystem paymentSystem;

    @Autowired
    private LeasingController leasingController;

    @Autowired
    private NotificationGate notificationGate;

    @Autowired
    private LeasingControlInteractor interactor;

    private final Period shortPeriod = Period.ofDays(1);

    private final Period longPeriod = Period.ofDays(3);

    private final Duration durationMoreThanShortButLessThanLongPeriod = Duration.ofDays(2);

    @Test
    void testSendNotifications()
    throws InterruptedException {
        // given
        final Client client = getSampleClient();
        final Cell cellOne = vault.requestCell(CellSize.SMALL);
        final Cell cellTwo = vault.requestCell(CellSize.MEDIUM);
        leaseTwoCellsWithDifferentPeriods(client, cellOne, cellTwo);
        MOCKED_CLOCK.advanceBy(durationMoreThanShortButLessThanLongPeriod);
        // when
        Thread.sleep(2*LEASING_TIMERS_CHECK_PERIOD_MS);
        interactor.sendNotifications();
        // then
        verify(notificationGate).notifyClientAboutLeasingExpiration(client, cellOne);
        verify(notificationGate, never()).notifyClientAboutLeasingExpiration(client, cellTwo);
    }

    private Client getSampleClient() {
        final Client client = TestDataGenerator.getSampleClient();
        clientsRepository.add(client);
        return client;
    }

    private void leaseTwoCellsWithDifferentPeriods(Client client, Cell cellOne, Cell cellTwo) {
        leasingController.startLeasing(cellOne, client, shortPeriod);
        leasingController.startLeasing(cellTwo, client, longPeriod);
    }

    @Test
    void testContinueLeasing() {
        // given
        final Client client = getSampleClient();
        final Cell cell = vault.requestCell(CellSize.SMALL);
        final Period newLeasePeriod = Period.ofDays(120);
        // when
        final Invoice invoice = interactor.continueLeasing(client, cell, newLeasePeriod);
        // then
        final CellApplication application = applicationsRepository.find(paymentSystem.findGood(invoice));
        assertThat(application.getStatus()).isEqualTo(CellApplicationStatus.APPROVED);
        assertThat(application.getCell()).isEqualTo(cell);
        assertThat(application.getLeaseholder()).isEqualTo(client);
        assertThat(application.getLeasePeriod()).isEqualTo(newLeasePeriod);
        assertThat(invoice.isPaid()).isFalse();
        assertThat(invoice.getSum()).isEqualTo(application.calculateLeaseCost());
    }

    @ParameterizedTest
    @EnumSource(PaymentMethod.class)
    void testAcceptPayment(PaymentMethod paymentMethod)
    throws InterruptedException {
        // given
        final CellApplication cellApplication = createApprovedCellApplication();
        leasingController.startLeasing(cellApplication.getCell(),
                cellApplication.getLeaseholder(), cellApplication.getLeasePeriod());
        MOCKED_CLOCK.advanceBy(Duration.ofDays(33));
        Thread.sleep(2*LEASING_TIMERS_CHECK_PERIOD_MS);
        Assumptions.assumeTrue(leasingController.isLeasingExpired(cellApplication.getCell()));
        final Invoice invoice = paymentSystem.issueInvoice(
                cellApplication.calculateLeaseCost(), cellApplication.getId());
        paymentSystem.pay(invoice, invoice.getSum(), paymentMethod);
        // when
        interactor.acceptPayment(invoice);
        // then
        assertThat(cellApplication.getStatus()).isEqualTo(CellApplicationStatus.PAID);
        assertFalse(leasingController.isLeasingExpired(cellApplication.getCell()));
    }

    private CellApplication createApprovedCellApplication() {
        final CellApplication cellApplication =
                TestDataGenerator.getCellApplication(CellApplicationStatus.APPROVED);
        cellsRepository.save(cellApplication.getCell());
        clientsRepository.add(cellApplication.getLeaseholder());
        applicationsRepository.save(cellApplication);
        return cellApplication;
    }

    @Test
    void testStopLeasing_EmptyCell() {
        // given
        final Cell cell = vault.requestAnyCell();
        final Client client = TestDataGenerator.getSampleClient();
        leasingController.startLeasing(cell, client, Period.ofMonths(1));
        // when
        interactor.stopLeasing(client, cell);
        // then
        assertFalse(leasingController.isLeased(cell));
        verify(notificationGate).notifyClient(eq(client), anyString());
    }

    @Test
    void testStopLeasing_NotEmptyCell() {
        // given
        final Cell cell = vault.requestAnyCell();
        final Client client = TestDataGenerator.getSampleClient();
        final Precious precious = new Precious(1, "");
        cell.setContainedPrecious(precious);
        // when
        interactor.stopLeasing(client, cell);
        // then
        verify(notificationGate).notifyManagerAboutLeasingEnd(cell);
    }

    @Test
    void testArrangeLeasingEnd() {
        // given
        final Client client = TestDataGenerator.getSampleClient();
        final LocalDate preciousExtractionDay = LocalDate.now();
        // when
        interactor.arrangeLeasingEnd(client, preciousExtractionDay);
        // then
        verify(notificationGate).notifyClientAboutArrangement(
                eq(client), anyString(), eq(preciousExtractionDay));
    }

    @BeforeEach
    void setUp() {
        leasingController.setTimersCheckPeriodMillis(LEASING_TIMERS_CHECK_PERIOD_MS);
    }

    @Configuration
    static class Config {
        @Bean
        LeasingController leasingController(final CellsRepository cellsRepository) {
            return new LeasingController(MOCKED_CLOCK, cellsRepository);
        }
    }
}
