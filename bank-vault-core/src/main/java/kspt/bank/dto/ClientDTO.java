package kspt.bank.dto;

import lombok.Value;

import java.time.LocalDate;

@Value
public class ClientDTO {
    public final String passportSerial;

    public final String firstName;

    public final String lastName;

    public final String patronymic;

    public final LocalDate birthday;

    public final String email;

    public final String phone;
}
