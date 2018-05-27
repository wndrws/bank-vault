package kspt.bank.domain.bp;

import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.dao.DatabaseApplicationsRepository;
import kspt.bank.dao.DatabaseClientsRepository;
import kspt.bank.enums.CellApplicationStatus;
import kspt.bank.enums.CellSize;
import kspt.bank.enums.PaymentMethod;
import kspt.bank.external.Invoice;
import kspt.bank.external.PaymentGate;
import kspt.bank.domain.*;
import kspt.bank.domain.entities.*;
import kspt.bank.external.SimplePaymentSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.reflect.Field;
import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplyForCellTest extends TestUsingDatabase {
    private final ClientsRepository clientsRepository = new DatabaseClientsRepository();

    private final ApplicationsRepository applicationsRepository = new DatabaseApplicationsRepository();

    private final PaymentGate paymentGate = new SimplePaymentSystem();

    private final CellApplicationInteractor caInteractor =
            new CellApplicationInteractor(clientsRepository, applicationsRepository, paymentGate);

    private final RoleClient roleClient = new RoleClient();

    private final RoleManager roleManager = new RoleManager();

    private CellApplication cellApplication;

    private Invoice invoice;

    @ParameterizedTest
    @ArgumentsSource(LeaseVariantsProvider.class)
    void testBusinessProcess(CellSize cellSize, int numOfMonths) {
        cellApplication = roleClient.initialApply();
        assertExistenceOfClientAndCellApplication();

        roleClient.requestCell(cellSize, Period.ofMonths(numOfMonths));
        assertThatRightCellIsReserved(cellSize, Period.ofMonths(numOfMonths));

        invoice = roleManager.approve();
        assertInvoiceAndApprovalOfCellApplication();

        roleClient.pay(invoice);
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
        assertFalse(Vault.getInstance().getLeasingController().isLeased(cellApplication.getCell()));
        assertThat(cellApplication.getCell().getSize()).isEqualTo(size);
        assertThat(cellApplication.getLeasePeriod()).isEqualTo(period);
        assertThat(cellApplication.getStatus()).isEqualTo(CellApplicationStatus.CELL_CHOSEN);
    }

    private void assertInvoiceAndApprovalOfCellApplication() {
        assertFalse(invoice.isPaid());
        assertThat(cellApplication.getStatus()).isEqualTo(CellApplicationStatus.APPROVED);
    }

    private void assertThatCellIsLeased() {
        assertTrue(invoice.isPaid());
        assertTrue(Vault.getInstance().getLeasingController().isLeased(cellApplication.getCell()));
        assertThat(cellApplication.getStatus()).isEqualTo(CellApplicationStatus.PAID);
    }

    private class RoleClient {
        final PassportInfo passportInfo = TestDataGenerator.getCorrectPassportInfo();

        final String email = "johnwick@example.com";

        final String phone = "+11231237777";

        CellApplication initialApply() {
            return caInteractor.createApplication(passportInfo, phone, email);
        }

        void requestCell(CellSize size, Period period) {
            caInteractor.requestCell(size, period, cellApplication);
        }

        void pay(Invoice invoice) {
            paymentGate.pay(invoice, invoice.getSum(), PaymentMethod.CASH);
            caInteractor.acceptPayment(invoice);
        }
    }

    private class RoleManager {
        Invoice approve() {
            return caInteractor.approveApplication(cellApplication);
        }
    }

    @BeforeEach
    private void resetSingleton()
    throws Exception {
        Field instance = Vault.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

}
