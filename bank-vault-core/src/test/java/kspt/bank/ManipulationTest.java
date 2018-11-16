package kspt.bank;

import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.boundaries.NotificationGate;
import kspt.bank.config.VaultConfig;
import kspt.bank.dao.JpaCellsRepository;
import kspt.bank.domain.*;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.ManipulationLog;
import kspt.bank.domain.entities.Precious;
import kspt.bank.enums.CellSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@AutoConfigurationPackage
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {
        Vault.class, VaultConfig.class, CellManipulationInteractor.class,
        ManipulationTest.Config.class })
class ManipulationTest {
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
    private JpaCellsRepository cellsRepository;

    @Autowired
    private CellManipulationInteractor cmInteractor;

    private final RoleClient roleClient = new RoleClient();

    private final RoleOperator roleOperator = new RoleOperator();

    private Cell smallCell;

    private Cell mediumCell;

    private Cell bigCell;

    private Cell cell;

    @ParameterizedTest
    @EnumSource(CellSize.class)
    void testBusinessProcess(final CellSize cellSize) {
        cell = roleClient.selectCell(cellSize);
        assertThatCorrectCellIsReturned(cellSize);

        assertThatCellIsClosed();
        roleOperator.openCell();
        assertThatCellIsOpened();

        assertThatCellIsEmpty();
        roleClient.putPrecious();
        assertThatCellContainsThePrecious();

        final Precious precious = roleClient.getPrecious();
        assertThatCorrectPreciousIsReturned(precious);
        assertThatCellIsEmpty();

        assertThatCellIsOpened();
        roleOperator.closeCell();
        assertThatCellIsClosed();

        roleOperator.notifyManager();
        verifyThatManagerIsNotified();
    }

    private void assertThatCorrectPreciousIsReturned(Precious precious) {
        assertThat(precious).isEqualTo(roleClient.precious);
    }

    private void verifyThatManagerIsNotified() {
        verify(notificationGate).notifyManager(anyString());
    }

    private void assertThatCellIsEmpty() {
        assertTrue(cell.isEmpty());
    }

    private void assertThatCellContainsThePrecious() {
        assertThat(cell.getContainedPrecious()).isEqualTo(roleClient.precious);
    }

    private void assertThatCellIsOpened() {
        assertTrue(vaultHardware.isOpened(cell));
    }

    private void assertThatCellIsClosed() {
        assertFalse(vaultHardware.isOpened(cell));
    }

    private void assertThatCorrectCellIsReturned(final CellSize selectedCellNum) {
        switch (selectedCellNum) {
            case SMALL: assertThat(cell).isEqualTo(smallCell); break;
            case MEDIUM: assertThat(cell).isEqualTo(mediumCell); break;
            case BIG: assertThat(cell).isEqualTo(bigCell); break;
            default: throw new IllegalArgumentException();
        }
    }

    private class RoleClient {
        final Client client = TestDataGenerator.getSampleClient();

        final Precious precious = new Precious(1, "The Ring Of Power");

        Cell selectCell(CellSize size) {
            return cmInteractor.getClientsCells(client).stream()
                    .filter(cell -> cell.getSize() == size)
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
        }

        void putPrecious() {
            cmInteractor.putPrecious(cell, precious, client);
        }

        Precious getPrecious() {
            return cmInteractor.getPrecious(cell, client);
        }
    }

    private class RoleOperator {
        void openCell() {
            cmInteractor.openCell(cell, roleClient.client);
        }

        void closeCell() {
            cmInteractor.closeCell(cell, roleClient.client);
        }

        void notifyManager() {
            cmInteractor.notifyAboutManipulation(cell, roleClient.client);
        }
    }

    @BeforeEach
    void setUp() {
        smallCell = vault.requestCell(CellSize.SMALL);
        mediumCell = vault.requestCell(CellSize.MEDIUM);
        bigCell = vault.requestCell(CellSize.BIG);
        clientsRepository.add(roleClient.client);
        leasingController.startLeasing(smallCell, roleClient.client, Period.ofMonths(1));
        leasingController.startLeasing(mediumCell, roleClient.client, Period.ofMonths(2));
        leasingController.startLeasing(bigCell, roleClient.client, Period.ofMonths(3));
    }


    @Configuration
    static class Config {
        @MockBean
        NotificationGate notificationGate;

        @MockBean
        ManipulationLog manipulationLog;
    }
}
