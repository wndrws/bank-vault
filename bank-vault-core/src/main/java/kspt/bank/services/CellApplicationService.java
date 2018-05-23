package kspt.bank.services;

import kspt.bank.domain.CellApplicationInteractor;
import kspt.bank.domain.entities.CellApplication;
import kspt.bank.domain.entities.CellSize;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.PassportInfo;

import java.time.LocalDate;
import java.time.Period;

// No Lombok here to interoperate with Kotlin
public class CellApplicationService {
    private final CellApplicationInteractor cellApplicationInteractor;

    public CellApplicationService(CellApplicationInteractor cellApplicationInteractor) {
        this.cellApplicationInteractor = cellApplicationInteractor;
    }

    public Integer acceptClientInfo(String serial, String firstName, String lastName,
            String patronymic, LocalDate birthday, String phone, String email) {
        final PassportInfo passportInfo = new PassportInfo(
                serial, firstName, lastName, patronymic, birthday);
        final CellApplication cellApplication = cellApplicationInteractor.createApplication(
                passportInfo, phone, email);
        return cellApplication.getId();
    }

    public Boolean requestCell(CellSize size, Period leasePeriod, Integer cellApplicationId) {
        return cellApplicationInteractor.requestCell(size, leasePeriod,
                cellApplicationInteractor.getApplicationsRepository().find(cellApplicationId));
    }
}
