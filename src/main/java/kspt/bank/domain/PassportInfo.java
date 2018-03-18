package kspt.bank.domain;

import lombok.Value;

import java.time.LocalDate;

@Value
public class PassportInfo {
    String serial;

    String firstName;

    String lastName;

    String patronymic;

    LocalDate birthDate;
}