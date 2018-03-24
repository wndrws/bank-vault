package kspt.bank.domain;

import kspt.bank.domain.entities.PassportInfo;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;

@UtilityClass
public class PassportInfoGenerator {
    public PassportInfo getCorrect() {
        return new PassportInfo("0409756123", "John", "Wick",
                "", LocalDate.of(1980, 1, 1));
    }

    public PassportInfo getWithIncorrectSerial() {
        return new PassportInfo("123","John", "Wick",
                "", LocalDate.of(1980, 1, 1));
    }

    public PassportInfo getWithIncorrectFirstName() {
        return new PassportInfo("0409756123","", "Wick",
                "", LocalDate.of(1980, 1, 1));
    }

    public PassportInfo getWithIncorrectLastName() {
        return new PassportInfo("0409756123","John", "",
                "", LocalDate.of(1980, 1, 1));
    }
}
