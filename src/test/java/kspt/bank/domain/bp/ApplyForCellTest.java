package kspt.bank.domain.bp;

import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.boundaries.PaymentGate;
import kspt.bank.domain.*;
import kspt.bank.domain.entities.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ApplyForCellTest {
    private final ClientsRepository clientsRepository = new InMemoryClientsRepository();

    private final ApplicationsRepository applicationsRepository = new InMemoryApplicationsRepository();

    private final PaymentGate paymentGate = mock(PaymentGate.class);

    private final CellApplicationInteractor caInteractor =
            new CellApplicationInteractor(clientsRepository, applicationsRepository, paymentGate);

    private final RoleClient roleClient = new RoleClient();

    private final RoleManager roleManager = new RoleManager();

    private CellApplication cellApplication;

    @ParameterizedTest
    @ArgumentsSource(LeaseVariantsProvider.class)
    void test(CellSize cellSize, int numOfMonths) {
        roleClient.initialApply();
        assertExistenceOfClientAndCellApplication();

        roleClient.requestCell(cellSize, Period.ofMonths(numOfMonths));
        assertThatRightCellIsReserved(cellSize, Period.ofMonths(numOfMonths));

        final long sum = roleManager.approve();
        assertApprovalOfCellApplication(sum);

        roleClient.pay(sum);
        assertThatCellIsLeased();
    }

    private void assertExistenceOfClientAndCellApplication() {
        assertTrue(clientsRepository.containsClientWith(roleClient.passportInfo));
        final Client client = clientsRepository.getClientWith(roleClient.passportInfo);
        assertThat(applicationsRepository.getByClient(client)).contains(cellApplication);
        assertThat(cellApplication.getStatus()).isEqualTo(CellApplicationStatus.CREATED);
    }


    private void assertThatRightCellIsReserved(CellSize size, Period period) {
        assertFalse(Vault.getInstance().isAvailable(cellApplication.getCell()));
        assertFalse(Vault.getInstance().isLeased(cellApplication.getCell()));
        assertThat(cellApplication.getCell().getSize()).isEqualTo(size);
        assertThat(cellApplication.getLeasePeriod()).isEqualTo(period);
        assertThat(cellApplication.getStatus()).isEqualTo(CellApplicationStatus.CELL_CHOSEN);
    }

    private void assertApprovalOfCellApplication(long sum) {
        assertThat(cellApplication.getLeaseCost()).isEqualTo(sum);
        assertThat(cellApplication.getStatus()).isEqualTo(CellApplicationStatus.APPROVED);
    }

    private void assertThatCellIsLeased() {
        assertTrue(Vault.getInstance().isLeased(cellApplication.getCell()));
        assertThat(cellApplication.getStatus()).isEqualTo(CellApplicationStatus.PAID);
    }

    private class RoleClient {
        final PassportInfo passportInfo = TestDataGenerator.getCorrectPassportInfo();

        final String email = "johnwick@example.com";

        final String phone = "+11231237777";

        void initialApply() {
            cellApplication = caInteractor.createApplication(passportInfo, phone, email);
        }

        void requestCell(CellSize size, Period period) {
            caInteractor.requestCell(size, period, cellApplication);
        }

        void pay(long sum) {
            caInteractor.acceptPayment(sum, PaymentMethod.CASH, cellApplication);
        }
    }

    private class RoleManager {
        long approve() {
            return caInteractor.approveApplication(cellApplication);
        }
    }
}
