package kspt.bank.domain;

import com.statemachinesystems.mockclock.MockClock;
import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.NotificationGate;
import kspt.bank.domain.entities.*;
import kspt.bank.external.Invoice;
import kspt.bank.external.PaymentGate;
import kspt.bank.external.SimplePaymentSystem;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class LeasingControlInteractorTest {
    private final static long LEASING_TIMERS_CHECK_PERIOD_MS = 100;

    private final NotificationGate notificationGate = mock(NotificationGate.class);

    private final ApplicationsRepository applicationsRepository = mock(ApplicationsRepository.class);

    private final PaymentGate paymentGate = new SimplePaymentSystem();

    private final MockClock mockedClock =
            MockClock.at(2018, 4, 10, 19, 0, ZoneId.systemDefault());

    private final LeasingControlInteractor interactor = new LeasingControlInteractor(mockedClock,
            notificationGate, applicationsRepository, paymentGate);

    private final Period shortPeriod = Period.ofDays(1);

    private final Period longPeriod = Period.ofDays(3);

    private final Duration durationMoreThanShortButLessThanLongPeriod = Duration.ofDays(2);

    @Test
    void testSendNotifications()
    throws InterruptedException {
        // given
        final Client client = TestDataGenerator.getSampleClient();
        final Cell cellOne = Vault.getInstance().requestCell(CellSize.SMALL);
        final Cell cellTwo = Vault.getInstance().requestCell(CellSize.MEDIUM);
        leaseTwoCellsWithDifferentPeriods(client, cellOne, cellTwo);
        mockedClock.advanceBy(durationMoreThanShortButLessThanLongPeriod);
        // when
        Thread.sleep(2*LEASING_TIMERS_CHECK_PERIOD_MS);
        interactor.sendNotifications();
        // then
        verify(notificationGate).notifyClientAboutLeasingExpiration(client, cellOne);
        verify(notificationGate, never()).notifyClientAboutLeasingExpiration(client, cellTwo);
    }

    private void leaseTwoCellsWithDifferentPeriods(Client client, Cell cellOne, Cell cellTwo) {
        Vault.getInstance().getLeasingController().startLeasing(cellOne, client, shortPeriod);
        Vault.getInstance().getLeasingController().startLeasing(cellTwo, client, longPeriod);
    }

    @Test
    void testContinueLeasing() {
        // given
        final Client client = TestDataGenerator.getSampleClient();
        final Cell cell = Vault.getInstance().requestCell(CellSize.SMALL);
        final Period newLeasePeriod = Period.ofMonths(4);
        // when
        final Invoice invoice = interactor.continueLeasing(client, cell, newLeasePeriod);
        // then
        final CellApplication application = interactor.getInvoiceToApplicationMap().get(invoice);
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
        final CellApplication cellApplication =
                TestDataGenerator.getCellApplication(CellApplicationStatus.APPROVED);
        Vault.getInstance().getLeasingController().startLeasing(cellApplication.getCell(),
                cellApplication.getLeaseholder(), cellApplication.getLeasePeriod());
        mockedClock.advanceBy(Duration.ofDays(33));
        Thread.sleep(2*LEASING_TIMERS_CHECK_PERIOD_MS);
        Assumptions.assumeTrue(Vault.getInstance().getLeasingController()
                .isLeasingExpired(cellApplication.getCell()));
        final Invoice invoice = new Invoice(cellApplication.calculateLeaseCost());
        interactor.getInvoiceToApplicationMap().put(invoice, cellApplication);
        paymentGate.pay(invoice, invoice.getSum(), paymentMethod);
        // when
        interactor.acceptPayment(invoice);
        // then
        assertThat(cellApplication.getStatus()).isEqualTo(CellApplicationStatus.PAID);
        assertFalse(Vault.getInstance().getLeasingController()
                .isLeasingExpired(cellApplication.getCell()));
    }

    @Test
    void testStopLeasing_EmptyCell() {
        // given
        final Cell cell = Vault.getInstance().requestAnyCell();
        final Client client = TestDataGenerator.getSampleClient();
        Vault.getInstance().getLeasingController().startLeasing(cell, client, Period.ofMonths(1));
        // when
        interactor.stopLeasing(client, cell);
        // then
        assertFalse(Vault.getInstance().getLeasingController().isLeased(cell));
        verify(notificationGate).notifyClient(eq(client), anyString());
    }

    @Test
    void testStopLeasing_NotEmptyCell() {
        // given
        final Cell cell = Vault.getInstance().requestAnyCell();
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
    void setUp()
    throws Exception {
        resetSingleton();
        Vault.getInstance().getLeasingController()
                .setTimersCheckPeriodMillis(LEASING_TIMERS_CHECK_PERIOD_MS);
    }

    private void resetSingleton()
    throws Exception {
        Field instance = Vault.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }
}
