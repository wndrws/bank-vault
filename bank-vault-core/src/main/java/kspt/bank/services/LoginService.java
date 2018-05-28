package kspt.bank.services;

import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.PassportInfo;
import kspt.bank.dto.ClientDTO;
import kspt.bank.recognition.Credentials;
import kspt.bank.recognition.UserStorage;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class LoginService {
    @Autowired
    private final ClientsRepository clientsRepository;

    @Autowired
    private final BankVaultFacade bankVaultFacade;

    @Autowired
    private final UserStorage userStorage;

    @Autowired
    private final TransactionManager transactionManager;

    public Integer registerUser(Credentials userCredentials, ClientDTO clientInfo) {
        final PassportInfo passportInfo = new PassportInfo(
                clientInfo.passportSerial, clientInfo.firstName, clientInfo.lastName,
                clientInfo.patronymic, clientInfo.birthday);
        final Client newClient = new Client(passportInfo, clientInfo.phone, clientInfo.email);
        transactionManager.runTransactional(() -> {
            clientsRepository.add(newClient);
            userStorage.createUser(userCredentials, newClient.getId());
        });
        return newClient.getId();
    }

    public Optional<Integer> login(Credentials userCredentials) {
        return Optional.ofNullable(userStorage.findUser(userCredentials));
    }

    public Optional<ClientDTO> getUserInfo(Integer userId) {
        return bankVaultFacade.findClientInfo(userId);
    }
}
