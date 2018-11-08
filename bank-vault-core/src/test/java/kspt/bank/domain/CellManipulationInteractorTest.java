package kspt.bank.domain;

import kspt.bank.TestConfig;
import kspt.bank.boundaries.CellsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.boundaries.NotificationGate;
import kspt.bank.config.VaultConfig;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.ManipulationLog;
import kspt.bank.domain.entities.Precious;
import kspt.bank.enums.CellSize;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Clock;
import java.time.Period;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {
        CellManipulationInteractor.class, Vault.class, VaultConfig.class, TestConfig.class,
        CellManipulationInteractorTest.Config.class })
class CellManipulationInteractorTest {
    @Autowired
    private Vault vault;

    @Autowired
    private VaultHardware vaultHardware;

    @Autowired
    private LeasingController leasingController;

    @Autowired
    private ManipulationLog manipulationLog;

    @Autowired
    private NotificationGate notificationGate;

    @Autowired
    private ClientsRepository clientsRepository;

    @Autowired
    private CellManipulationInteractor interactor;

    private final Client client = TestDataGenerator.getSampleClient();

    private Cell cellOne;

    private Cell cellTwo;

    private Cell cellThree;

    private final Precious myPrecious = new Precious(1, "The Ring Of Power");

    private final Precious tooBigPrecious = new Precious(100, "Too Big Precious");

    @Test
    void testGetClientsCells() {
        // given
        leasingController.startLeasing(cellOne, client, Period.ofMonths(1));
        leasingController.startLeasing(cellTwo, client, Period.ofMonths(2));
        leasingController.startLeasing(cellThree, client, Period.ofMonths(3));
        // when
        final List<Cell> clientsCells = interactor.getClientsCells(client);
        // then
        assertThat(clientsCells).containsExactlyInAnyOrder(cellOne, cellTwo, cellThree);
    }

    @Test
    void testPutPrecious() {
        // given
        leasingController.startLeasing(cellOne, client, Period.ofMonths(1));
        // when
        interactor.putPrecious(cellOne, myPrecious, client);
        // then
        assertThat(cellOne.getContainedPrecious()).isEqualTo(myPrecious);
        verify(manipulationLog).logPutManipulation(anyString(), eq(client), eq(cellOne));
    }

    @Test
    void testPutPrecious_TooBigPrecious() {
        // given
        leasingController.startLeasing(cellThree, client, Period.ofMonths(3));
        // then
        assertThrows(PutManipulationValidator.ManipulationNotAllowed.class, // when
                () -> interactor.putPrecious(cellThree, tooBigPrecious, client));
    }

    @Test
    void testPutPrecious_NotEmptyCell() {
        // given
        leasingController.startLeasing(cellTwo, client, Period.ofMonths(2));
        cellThree.setContainedPrecious(myPrecious);
        // then
        assertThrows(PutManipulationValidator.ManipulationNotAllowed.class, // when
                () -> interactor.putPrecious(cellThree, new Precious(1, ""), client));
    }

    @Test
    void testGetPrecious() {
        // given
        cellTwo.setContainedPrecious(myPrecious);
        // when
        final Precious precious = interactor.getPrecious(cellTwo, client);
        // then
        assertThat(precious).isEqualTo(myPrecious);
        verify(manipulationLog).logGetManipulation(anyString(), eq(client), eq(cellTwo));
    }

    @Test
    void testOpenCell() {
        // given
        Assumptions.assumeFalse(vaultHardware.isOpened(cellOne));
        // when
        interactor.openCell(cellOne, client);
        // then
        assertTrue(vaultHardware.isOpened(cellOne));
        verify(manipulationLog).logEvent(anyString(), eq(client), eq(cellOne));
    }

    @Test
    void testCloseCell() {
        // given
        interactor.openCell(cellOne, client);
        Assumptions.assumeTrue(vaultHardware.isOpened(cellOne));
        // when
        interactor.closeCell(cellOne, client);
        // then
        assertFalse(vaultHardware.isOpened(cellOne));
        verify(manipulationLog, Mockito.times(2)).logEvent(anyString(), eq(client), eq(cellOne));
    }

    @Test
    void testNotifyAboutManipulation() {
        // when
        interactor.notifyAboutManipulation(cellOne, client);
        // then
        verify(notificationGate).notifyManager(anyString());
    }

    @BeforeEach
    void setUp() {
        cellOne = vault.requestCell(CellSize.SMALL);
        cellTwo = vault.requestCell(CellSize.MEDIUM);
        cellThree = vault.requestCell(CellSize.BIG);
        clientsRepository.add(client);
    }

    @Configuration
    static class Config {
        @Bean
        LeasingController leasingController(final Clock clock, final CellsRepository cellsRepository) {
            return new LeasingController(clock, cellsRepository);
        }
    }
}
