package kspt.bank.domain;

import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.boundaries.PaymentGate;
import kspt.bank.domain.ClientPassportValidator.IncorrectPassportInfo;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellSize;
import kspt.bank.domain.entities.PassportInfo;
import kspt.bank.domain.entities.PaymentMethod;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SuppressWarnings("ConstantConditions")
class CellApplicationInteractorTest {
    private final ClientsRepository clientsRepository = mock(ClientsRepository.class);

    private final PaymentGate paymentGate = mock(PaymentGate.class);

    private final CellApplicationInteractor interactor =
            new CellApplicationInteractor(clientsRepository, paymentGate);

    @Test
    void testAcceptClientInfo_NewClient() {
        // given
        final PassportInfo passportInfo = TestDataGenerator.getCorrectPassportInfo();
        when(clientsRepository.containsClientWith(passportInfo)).thenReturn(false);
        // when
        interactor.acceptClientInfo(passportInfo);
        // then
        verify(clientsRepository).addClientWith(passportInfo);
    }

    @Test
    void testAcceptClientInfo_ExistingClient() {
        // given
        final PassportInfo passportInfo = TestDataGenerator.getCorrectPassportInfo();
        when(clientsRepository.containsClientWith(passportInfo)).thenReturn(true);
        // when
        interactor.acceptClientInfo(passportInfo);
        // then
        verify(clientsRepository, never()).addClientWith(passportInfo);
    }

    @Test
    void testAcceptClientInfo_IncorrectSerial() {
        final PassportInfo userInfo = TestDataGenerator.getPassportInfoWithIncorrectSerial();
        assertThrows(IncorrectPassportInfo.class, () -> interactor.acceptClientInfo(userInfo));
    }

    @Test
    void testAcceptClientInfo_IncorrectFirstName() {
        final PassportInfo userInfo = TestDataGenerator.getPassportInfoWithIncorrectFirstName();
        assertThrows(IncorrectPassportInfo.class, () -> interactor.acceptClientInfo(userInfo));
    }

    @Test
    void testAcceptClientInfo_IncorrectLastName() {
        final PassportInfo userInfo = TestDataGenerator.getPassportInfoWithIncorrectLastName();
        assertThrows(IncorrectPassportInfo.class, () -> interactor.acceptClientInfo(userInfo));
    }

    @ParameterizedTest
    @ArgumentsSource(CellSizesWithTotalsProvider.class)
    void testRequestCellOfSize(CellSize size, int totalCellsOfThatSize) {
        // given
        Assumptions.assumeTrue(totalCellsOfThatSize > 0);
        // when
        final Optional<Cell> optionalCell = interactor.requestCellOfSize(size);
        // then
        assertTrue(optionalCell.isPresent());
    }

    @ParameterizedTest
    @EnumSource(PaymentMethod.class)
    void testAcceptPayment_PositiveSum(PaymentMethod paymentMethod) {
        final long sum = new Random().longs(1L, Long.MAX_VALUE).findFirst().getAsLong();
        interactor.acceptPayment(sum, paymentMethod);
    }

    @ParameterizedTest
    @EnumSource(PaymentMethod.class)
    void testAcceptPayment_ZeroSum(PaymentMethod paymentMethod) {
        final long sum = 0;
        assertThrows(IllegalArgumentException.class, () -> interactor.acceptPayment(sum, paymentMethod));
    }

    @ParameterizedTest
    @EnumSource(PaymentMethod.class)
    void testAcceptPayment_NegativeSum(PaymentMethod paymentMethod) {
        final long sum = new Random().longs(Long.MIN_VALUE, -1L).findFirst().getAsLong();
        assertThrows(IllegalArgumentException.class, () -> interactor.acceptPayment(sum, paymentMethod));
    }
}
