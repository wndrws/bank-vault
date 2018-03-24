package kspt.bank.domain.usecases;

import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.boundaries.ResponseGate;
import kspt.bank.domain.BankVaultFacade;
import kspt.bank.domain.CellApplicationInteractor;
import kspt.bank.domain.PassportInfoGenerator;
import kspt.bank.domain.entities.CellSize;
import kspt.bank.domain.entities.PassportInfo;
import kspt.bank.domain.entities.PaymentMethod;
import kspt.bank.messaging.RequestWithCellChoice;
import kspt.bank.messaging.RequestWithClientInfo;
import kspt.bank.messaging.RequestWithPayment;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class ClientApplyForCellUsecaseTest {
    private final ClientsRepository clientsRepository = mock(ClientsRepository.class);

    private final ResponseGate responseGate = mock(ResponseGate.class);

    private final BankVaultFacade bankVault =
            new BankVaultFacade(new CellApplicationInteractor(clientsRepository), responseGate);

    @Test
    void testNormal_NewClient() {
        final PassportInfo clientPassportInfo = PassportInfoGenerator.getCorrect();
        when(clientsRepository.containsClientWith(clientPassportInfo)).thenReturn(false);
        // 1. Клиент обращается к системе для получения банковской ячейки.
        testNormal(clientPassportInfo);
        // 6. Если это первое обращение Клиента, то система выдает ему логин и пароль.
        verify(clientsRepository).addClientWith(clientPassportInfo);
    }

    @Test
    void testNormal_ExistingClient() {
        final PassportInfo clientPassportInfo = PassportInfoGenerator.getCorrect();
        when(clientsRepository.containsClientWith(clientPassportInfo)).thenReturn(true);
        // 1. Клиент обращается к системе для получения банковской ячейки.
        testNormal(clientPassportInfo);
        // 6. Если это первое обращение Клиента, то система выдает ему логин и пароль.
        verify(clientsRepository, never()).addClientWith(clientPassportInfo);
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

    void testAlternative_IncorrectPassportInfo() {

    }
}
