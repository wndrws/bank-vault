package kspt.bank.domain.usecases;

import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.boundaries.PaymentGate;
import kspt.bank.boundaries.ResponseGate;
import kspt.bank.domain.BankVaultFacade;
import kspt.bank.domain.CellApplicationInteractor;
import kspt.bank.domain.TestDataGenerator;
import kspt.bank.domain.Vault;
import kspt.bank.domain.entities.*;
import kspt.bank.messaging.RequestWithCellChoice;
import kspt.bank.messaging.RequestWithClientInfo;
import kspt.bank.messaging.RequestWithPayment;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Period;

import static org.mockito.Mockito.*;

class ClientApplyForCellUsecaseTest {
    private final ClientsRepository clientsRepository = mock(ClientsRepository.class);

    private final ApplicationsRepository applicationsRepository = mock(ApplicationsRepository.class);

    private final ResponseGate responseGate = mock(ResponseGate.class);

    private final PaymentGate paymentGate = mock(PaymentGate.class);

    private final BankVaultFacade bankVault = new BankVaultFacade(new CellApplicationInteractor(
            clientsRepository, applicationsRepository, paymentGate), responseGate);

    @Test
    @Disabled
    void testNormal_NewClient() {
        final PassportInfo clientPassportInfo = TestDataGenerator.getCorrectPassportInfo();
        when(clientsRepository.containsClientWith(clientPassportInfo)).thenReturn(false);
        // 1. Клиент обращается к системе для получения банковской ячейки.
        testNormal(clientPassportInfo);
        // 6. Если это первое обращение Клиента, то система выдает ему логин и пароль.
        //TODO verify(clientsRepository).add(eq(clientPassportInfo), any(), any());
    }

    @Test
    @Disabled
    void testNormal_ExistingClient() {
        final PassportInfo clientPassportInfo = TestDataGenerator.getCorrectPassportInfo();
        when(clientsRepository.containsClientWith(clientPassportInfo)).thenReturn(true);
        // 1. Клиент обращается к системе для получения банковской ячейки.
        testNormal(clientPassportInfo);
        // 6. Если это первое обращение Клиента, то система выдает ему логин и пароль.
        //TODO verify(clientsRepository, never()).add(eq(clientPassportInfo), any(), any());
    }

    private void testNormal(final PassportInfo clientPassportInfo) {
        // 2. Клиент предоставляет свои паспортные данные.
        final RequestWithClientInfo requestWithClientInfo =
                new RequestWithClientInfo(clientPassportInfo);
        bankVault.acceptClientInfo(requestWithClientInfo);
        verify(responseGate).notifyAsCompleted(requestWithClientInfo);
        // 3. Клиент выбирает необходимый размер ячейки.
        final RequestWithCellChoice requestWithCellChoice =
                new RequestWithCellChoice(CellSize.MEDIUM);
        bankVault.acceptCellChoice(requestWithCellChoice);
        verify(responseGate).notifyAsCompleted(requestWithCellChoice);
        // 4. Клиент оплачивает аренду: наличными или с помощью банковской карты.
        // 5. Система подтверждает оплату аренды.
        final RequestWithPayment requestWithPayment =
                new RequestWithPayment(200L, PaymentMethod.CASH);
        bankVault.acceptPayment(requestWithPayment);
        verify(responseGate).notifyAsCompleted(requestWithPayment);
    }

    @Test
    void testAlternative_IncorrectPassportInfo() {
        // Альтернатива: Некорректные паспортные данные
        final PassportInfo clientPassportInfo = TestDataGenerator.getPassportInfoWithIncorrectSerial();
        // 2. Клиент предоставляет свои паспортные данные.
        final RequestWithClientInfo requestWithClientInfo =
                new RequestWithClientInfo(clientPassportInfo);
        bankVault.acceptClientInfo(requestWithClientInfo);
        verify(responseGate).notifyAsFailed(eq(requestWithClientInfo), anyString());
    }

    @Test
    void testAlternative_NoSuitableCell() {
        // Альтернатива: Нет свободной ячейки подходящего размера
        leaseAllCellsOfSize(CellSize.BIG);
        // 3. Клиент выбирает необходимый размер ячейки.
        final RequestWithCellChoice requestWithCellChoice =
                new RequestWithCellChoice(CellSize.BIG);
        bankVault.acceptCellChoice(requestWithCellChoice);
        verify(responseGate).notifyAsFailed(eq(requestWithCellChoice), anyString());
    }

    private void leaseAllCellsOfSize(final CellSize size) {
        Cell cell = Vault.getInstance().requestCell(size);
        while (cell != null) {
            Vault.getInstance().startLeasing(cell, TestDataGenerator.getSampleClient(), Period.ofMonths(1));
            cell = Vault.getInstance().requestCell(size);
        }
    }

    @Test
    void testAlternative_FailedPayment() {
        // Альтернатива: Оплата не прошла
        doThrow(RuntimeException.class).when(paymentGate).acceptPayment(anyLong(), any());
        // 4. Клиент оплачивает аренду: наличными или с помощью банковской карты.
        final RequestWithPayment requestWithPayment =
                new RequestWithPayment(200L, PaymentMethod.CASH);
        bankVault.acceptPayment(requestWithPayment);
        verify(responseGate).notifyAsFailed(eq(requestWithPayment), any());
    }
}