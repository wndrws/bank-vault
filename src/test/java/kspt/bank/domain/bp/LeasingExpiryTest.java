package kspt.bank.domain.bp;

import com.statemachinesystems.mockclock.MockClock;
import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.NotificationGate;
import kspt.bank.domain.*;
import kspt.bank.domain.entities.*;
import kspt.bank.external.Invoice;
import kspt.bank.external.PaymentGate;
import kspt.bank.external.SimplePaymentSystem;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LeasingExpiryTest {
    private final ApplicationsRepository applicationsRepository = new InMemoryApplicationsRepository();

    private final PaymentGate paymentGate = new SimplePaymentSystem();

    private final NotificationGate notificationGate = mock(NotificationGate.class);

    private final MockClock mockedClock =
            MockClock.at(2018, 4, 10, 19, 0, ZoneId.systemDefault());

    private final LeasingControlInteractor lcInteractor = new LeasingControlInteractor(mockedClock,
            notificationGate, applicationsRepository, paymentGate);

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
        Vault.getInstance().getLeasingController().startLeasing(roleClient.cell, roleClient.client,
                roleClient.initialLeasingPeriod);
        Assumptions.assumeTrue(Vault.getInstance().getLeasingController().isLeased(roleClient.cell));
    }

    private void waitForLeasingExpiry()
    throws InterruptedException {
        mockedClock.advanceByDays(roleClient.initialLeasingPeriod.getDays() + 1);
        Thread.sleep(2 * Vault.getInstance().getLeasingController().getTimersCheckPeriodMillis());
    }

    private void assertThatLeasingExpired() {
        assertTrue(Vault.getInstance().getLeasingController().isLeasingExpired(roleClient.cell));
    }

    private void verifyThatClientIsNotifiedAboutExpiry() {
        lcInteractor.sendNotifications();
        verify(notificationGate).notifyClientAboutLeasingExpiration(
                roleClient.client, roleClient.cell);
    }

    private void assertContinuationOfLeasing() {
        assertTrue(invoice.isPaid());
        assertTrue(Vault.getInstance().getLeasingController().isLeased(roleClient.cell));
        assertFalse(Vault.getInstance().getLeasingController().isLeasingExpired(roleClient.cell));
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
        assertFalse(Vault.getInstance().getLeasingController().isLeased(roleClient.cell));
        verify(notificationGate).notifyClient(eq(roleClient.client), anyString());
    }

    private class RoleClient {
        final Client client = TestDataGenerator.getSampleClient();

        final Precious precious = new Precious(1, "The Ring Of Power");

        final Cell cell = Vault.getInstance().requestCell(CellSize.SMALL);

        final Period initialLeasingPeriod = Period.ofDays(10);

        final Period newLeasingPeriod = Period.ofMonths(2);

        Invoice continueLeasing() {
            return lcInteractor.continueLeasing(client, cell, newLeasingPeriod);
        }

        void pay(Invoice invoice) {
            paymentGate.pay(invoice, invoice.getSum(), PaymentMethod.CASH);
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
    void setUp()
    throws Exception {
        resetSingleton();
        Vault.getInstance().getLeasingController().setTimersCheckPeriodMillis(
                LeasingControlInteractorTest.LEASING_TIMERS_CHECK_PERIOD_MS);
    }

    private void resetSingleton()
    throws Exception {
        Field instance = Vault.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }
}
