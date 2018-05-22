package kspt.bank.services;

import kspt.bank.domain.CellApplicationInteractor;
import kspt.bank.domain.entities.PassportInfo;

import java.time.LocalDate;

// No Lombok here to interoperate with Kotlin
public class ClientInfoService {
    private final CellApplicationInteractor cellApplicationInteractor;

    public ClientInfoService(CellApplicationInteractor cellApplicationInteractor) {
        this.cellApplicationInteractor = cellApplicationInteractor;
    }

    public void acceptClientInfo(String serial, String firstName, String lastName,
            String patronymic, LocalDate birthday, String phone, String email) {
        final PassportInfo passportInfo = new PassportInfo(
                serial, firstName, lastName, patronymic, birthday);
        cellApplicationInteractor.createApplication(passportInfo, phone, email);
    }
}
