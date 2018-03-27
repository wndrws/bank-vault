package kspt.bank.domain;

import kspt.bank.domain.entities.CellApplication;
import kspt.bank.domain.entities.CellApplicationStatus;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.PassportInfo;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.Period;

@UtilityClass
public class TestDataGenerator {
    public PassportInfo getCorrectPassportInfo() {
        return new PassportInfo("0409756123", "John", "Wick",
                "", LocalDate.of(1980, 1, 1));
    }

    public PassportInfo getPassportInfoWithIncorrectSerial() {
        return new PassportInfo("123","John", "Wick",
                "", LocalDate.of(1980, 1, 1));
    }

    public PassportInfo getPassportInfoWithIncorrectFirstName() {
        return new PassportInfo("0409756123","", "Wick",
                "", LocalDate.of(1980, 1, 1));
    }

    public PassportInfo getPassportInfoWithIncorrectLastName() {
        return new PassportInfo("0409756123","John", "",
                "", LocalDate.of(1980, 1, 1));
    }

    public Client getSampleClient() {
        return new Client(getCorrectPassportInfo(), "", "");
    }

    public CellApplication getSampleCellApplication() {
        return new CellApplication(getSampleClient());
    }

    public CellApplication getCellApplication(final CellApplicationStatus status) {
        final CellApplication application = new CellApplication(getSampleClient());
        application.setCell(Vault.getInstance().requestAnyCell());
        application.setLeasePeriod(Period.ofMonths(1));
        application.setStatus(status);
        return application;
    }
}
