package kspt.bank.domain;

import com.statemachinesystems.mockclock.MockClock;
import kspt.bank.boundaries.CellsRepository;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.Client;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Period;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

public class LeasingControllerTest {
    @Autowired // TODO ?
    private Vault vault;

    @MockBean
    private CellsRepository cellsRepository;

    private final MockClock mockedClock =
            MockClock.at(2018, 3, 16, 17, 0, ZoneId.systemDefault());

    private final LeasingController leasingController = new LeasingController(mockedClock, cellsRepository);

    @Test
    void testLeasingExpiration()
    throws InterruptedException {
        Assumptions.assumeTrue(leasingController.getTimersCheckPeriodMillis() <= 1000L,
                "LeasingController's timers check period is inappropriate for unit-testing");
        // given
        final Cell cell = vault.requestAnyCell();
        final Client client = TestDataGenerator.getSampleClient();
        final Period leasingPeriod = Period.ofMonths(2);
        Assumptions.assumeFalse(leasingController.isLeased(cell));
        // when
        leasingController.startLeasing(cell, client, leasingPeriod);
        // then
        assertThat(leasingController.isLeased(cell)).isTrue();
        assertThat(leasingController.isLeasingExpired(cell)).isFalse();
        // and when
        mockedClock.advanceByDays(64);
        Thread.sleep(2*leasingController.getTimersCheckPeriodMillis());
        // then
        assertThat(leasingController.isLeased(cell)).isTrue();
        assertThat(leasingController.isLeasingExpired(cell)).isTrue();
    }
}
