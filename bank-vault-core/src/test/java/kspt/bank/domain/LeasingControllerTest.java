package kspt.bank.domain;

import com.statemachinesystems.mockclock.MockClock;
import kspt.bank.InMemoryConfig;
import kspt.bank.boundaries.CellsRepository;
import kspt.bank.config.VaultConfig;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.Client;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Period;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = { Vault.class, VaultConfig.class, InMemoryConfig.class })
class LeasingControllerTest {
    @Autowired
    private Vault vault;

    @Autowired
    private CellsRepository cellsRepository;

    private final MockClock mockedClock =
            MockClock.at(2018, 3, 16, 17, 0, ZoneId.systemDefault());

    private LeasingController leasingController;

    @BeforeEach
    void setUp() {
        leasingController = new LeasingController(mockedClock, cellsRepository);
    }

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
