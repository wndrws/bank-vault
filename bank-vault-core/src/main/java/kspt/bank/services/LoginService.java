package kspt.bank.services;

import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.domain.ClientPassportValidator;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.PassportInfo;
import kspt.bank.dto.ClientDTO;
import kspt.bank.recognition.Credentials;
import kspt.bank.recognition.UserStorage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class LoginService {
    @Autowired
    private final ClientsRepository clientsRepository;

    @Autowired
    private final BankVaultFacade bankVaultFacade;

    @Autowired
    private final UserStorage userStorage;

    @Transactional
    public Integer registerUser(Credentials userCredentials, ClientDTO clientInfo) {
        final PassportInfo passportInfo = new PassportInfo(
                clientInfo.passportSerial, clientInfo.firstName, clientInfo.lastName,
                clientInfo.patronymic, clientInfo.birthday);
        ClientPassportValidator.checkValidity(passportInfo);
        final Client newClient = new Client(passportInfo, clientInfo.phone, clientInfo.email);
        saveNewClient(newClient);
        userStorage.createUser(userCredentials, newClient.getId());
        return newClient.getId();
    }

    private void saveNewClient(Client newClient) {
        if (clientsRepository.containsClientWith(newClient.getPassportInfo())) {
            throw new ClientPassportValidator.IncorrectPassportInfo("Person with the passport" +
                    "having this serial is already registered!");
        }
        clientsRepository.add(newClient);
    }

    public Optional<Integer> login(Credentials userCredentials) {
        return Optional.ofNullable(userStorage.findUser(userCredentials));
    }

    public Optional<ClientDTO> getUserInfo(Integer userId) {
        return bankVaultFacade.findClientInfo(userId);
    }

    public boolean isRegistered(final String username) {
        return userStorage.containsUserWithName(username);
    }
}
