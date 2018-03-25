package kspt.bank.domain;

import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.boundaries.PaymentGate;
import kspt.bank.domain.ClientPassportValidator.IncorrectPassportInfo;
import kspt.bank.domain.entities.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Period;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SuppressWarnings("ConstantConditions")
class CellApplicationInteractorTest {
    private final ClientsRepository clientsRepository = mock(ClientsRepository.class);

    private final ApplicationsRepository applicationsRepository = mock(ApplicationsRepository.class);

    private final PaymentGate paymentGate = mock(PaymentGate.class);

    private final CellApplicationInteractor interactor =
            new CellApplicationInteractor(clientsRepository, applicationsRepository, paymentGate);

    @Test
    void testCreateApplication_NewClient() {
        // given
        final Client client = TestDataGenerator.getSampleClient();
        when(clientsRepository.containsClientWith(client.getPassportInfo())).thenReturn(false);
        // when
        final CellApplication application = interactor.createApplication(
                client.getPassportInfo(), client.getPhone(), client.getEmail());
        // then
        assertThat(application.getLeaseholder()).isEqualToIgnoringGivenFields(client, "id");
        assertThat(application.getStatus()).isEqualTo(CellApplicationStatus.CREATED);
        verify(applicationsRepository).add(application);
        verify(clientsRepository).add(argThat(c -> c.equalsIgnoringId(client)));
    }



    @Test
    void testCreateApplication_ExistingClient() {
        // given
        final Client client = TestDataGenerator.getSampleClient();
        final PassportInfo passportInfo = client.getPassportInfo();
        when(clientsRepository.containsClientWith(passportInfo)).thenReturn(true);
        when(clientsRepository.getClientWith(passportInfo)).thenReturn(client);
        // when
        final CellApplication application = interactor.createApplication(
                client.getPassportInfo(), client.getPhone(), client.getEmail());
        // then
        assertThat(application.getLeaseholder()).isEqualTo(client);
        assertThat(application.getStatus()).isEqualTo(CellApplicationStatus.CREATED);
        verify(applicationsRepository).add(application);
        verify(clientsRepository, never()).add(any());
    }

    @Test
    void testCreateApplication_IncorrectSerial() {
        final PassportInfo userInfo = TestDataGenerator.getPassportInfoWithIncorrectSerial();
        assertThrows(IncorrectPassportInfo.class, () -> interactor.createApplication(userInfo, "", ""));
    }

    @Test
    void testCreateApplication_IncorrectFirstName() {
        final PassportInfo userInfo = TestDataGenerator.getPassportInfoWithIncorrectFirstName();
        assertThrows(IncorrectPassportInfo.class, () -> interactor.createApplication(userInfo, "", ""));
    }

    @Test
    void testCreateApplication_IncorrectLastName() {
        final PassportInfo userInfo = TestDataGenerator.getPassportInfoWithIncorrectLastName();
        assertThrows(IncorrectPassportInfo.class, () -> interactor.createApplication(userInfo, "", ""));
    }

    @ParameterizedTest
    @ArgumentsSource(CellSizesWithTotalsProvider.class)
    void testRequestCellOfSize(CellSize size, int totalCellsOfThatSize) {
        // given
        Assumptions.assumeTrue(totalCellsOfThatSize > 0);
        final Period leasePeriod = Period.ofMonths(1);
        final CellApplication cellApplication = TestDataGenerator.getSampleCellApplication();
        // when
        final boolean success = interactor.requestCell(size, leasePeriod, cellApplication);
        // then
        assertThat(success).isTrue();
        assertThat(cellApplication.getCell()).isNotNull();
        assertThat(cellApplication.getLeasePeriod()).isEqualTo(leasePeriod);
        assertThat(cellApplication.getStatus()).isEqualTo(CellApplicationStatus.CELL_CHOSEN);
    }

    @ParameterizedTest
    @EnumSource(PaymentMethod.class)
    void testAcceptPayment_PositiveSum(PaymentMethod paymentMethod) {
        // given
        final CellApplication cellApplication =
                TestDataGenerator.getCellApplication(CellApplicationStatus.APPROVED);
        final long sum = new Random().longs(1L, Long.MAX_VALUE).findFirst().getAsLong();
        cellApplication.setLeaseCost(sum);
        // when
        interactor.acceptPayment(sum, paymentMethod, cellApplication);
        // then
        assertThat(cellApplication.getStatus()).isEqualTo(CellApplicationStatus.PAID);
        assertTrue(Vault.getInstance().isLeased(cellApplication.getCell()));
    }

    @ParameterizedTest
    @EnumSource(PaymentMethod.class)
    void testAcceptPayment_ZeroSum(PaymentMethod paymentMethod) {
        // given
        final CellApplication cellApplication =
                TestDataGenerator.getCellApplication(CellApplicationStatus.APPROVED);
        // when
        final long sum = 0;
        // then
        assertThrows(IllegalArgumentException.class,
                () -> interactor.acceptPayment(sum, paymentMethod, cellApplication));
    }

    @ParameterizedTest
    @EnumSource(PaymentMethod.class)
    void testAcceptPayment_NegativeSum(PaymentMethod paymentMethod) {
        // given
        final CellApplication cellApplication =
                TestDataGenerator.getCellApplication(CellApplicationStatus.APPROVED);
        // when
        final long sum = new Random().longs(Long.MIN_VALUE, -1L).findFirst().getAsLong();
        // then
        assertThrows(IllegalArgumentException.class,
                () -> interactor.acceptPayment(sum, paymentMethod, cellApplication));
    }
}
