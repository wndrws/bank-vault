package kspt.bank.domain.bp;

import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.boundaries.PaymentGate;
import kspt.bank.domain.*;
import kspt.bank.domain.entities.CellApplication;
import kspt.bank.domain.entities.CellSize;
import kspt.bank.domain.entities.PassportInfo;
import kspt.bank.domain.entities.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void test() {
        roleClient.initialApply();
        roleClient.requestCell(CellSize.MEDIUM, Period.ofMonths(1));
        final long sum = roleManager.approve();
        roleClient.pay(sum);
        assertSystemState();
    }

    private void assertSystemState() {
        // В базе клиентов есть данные о клиенте
        // Ячейка арендована согласно заявке клиента
        // ???
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
