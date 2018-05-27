package kspt.bank.domain.usecases;

import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.dao.InMemoryApplicationsRepository;
import kspt.bank.dao.InMemoryClientsRepository;
import kspt.bank.domain.*;
import kspt.bank.enums.CellApplicationStatus;
import kspt.bank.enums.CellSize;
import kspt.bank.enums.PaymentMethod;
import kspt.bank.external.Invoice;
import kspt.bank.external.PaymentGate;
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

/*
    На данный момент не предусмотрено тестов для других сценариев использования,
    так как они полностью покрываются тестами для бизнес-процессов с одной стороны
    и модульными тестами классов, реализующими требуемую функциональность, - с другой.

    Создание дополнительных тестов для каждого из сценариев приведет лишь к дублированию кода,
    чего хотелось бы избежать, и конкретно этот класс будет удален при первой возможности.
 */
class ClientApplyForCellUsecaseTest {
    private final ClientsRepository clientsRepository = new InMemoryClientsRepository();

    private final ApplicationsRepository applicationsRepository = new InMemoryApplicationsRepository();

    private final PaymentGate paymentGate = new SimplePaymentSystem();

    private final CellApplicationInteractor caInteractor =
            new CellApplicationInteractor(clientsRepository, applicationsRepository, paymentGate);

    private final PassportInfo passportInfo = TestDataGenerator.getCorrectPassportInfo();

    private CellApplication cellApplication;

    private Invoice invoice;

    @ParameterizedTest
    @ArgumentsSource(LeaseVariantsProvider.class)
    void testUsecase(CellSize cellSize, int numOfMonths) {
        cellApplication = caInteractor.createApplication(TestDataGenerator.getCorrectPassportInfo(),
                "+11231237777", "johnwick@example.com");
        assertExistenceOfClientAndCellApplication();

        caInteractor.requestCell(cellSize, Period.ofMonths(numOfMonths), cellApplication);
        assertThatRightCellIsReserved(cellSize, Period.ofMonths(numOfMonths));

        ensureApprovalOfCellApplication();

        paymentGate.pay(invoice, invoice.getSum(), PaymentMethod.CASH);
        caInteractor.acceptPayment(invoice);
        assertThatCellIsLeased();
    }

    private void assertExistenceOfClientAndCellApplication() {
        assertTrue(clientsRepository.containsClientWith(passportInfo));
        final Client client = clientsRepository.getClientWith(passportInfo);
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

    private void ensureApprovalOfCellApplication() {
        invoice = caInteractor.approveApplication(cellApplication);
    }

    private void assertThatCellIsLeased() {
        assertTrue(invoice.isPaid());
        assertTrue(Vault.getInstance().getLeasingController().isLeased(cellApplication.getCell()));
        assertThat(cellApplication.getStatus()).isEqualTo(CellApplicationStatus.PAID);
    }

    @BeforeEach
    void resetSingleton()
    throws Exception {
        Field instance = Vault.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }
}
