package kspt.bank.domain;

import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.PassportInfo;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;

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
        return new Client(1, getCorrectPassportInfo(), "", "");
    }
}
