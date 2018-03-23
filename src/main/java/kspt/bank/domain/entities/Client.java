package kspt.bank.domain.entities;

import lombok.Value;

@Value
public class Client {
    int id;

    PassportInfo passportInfo;

    String phone;

    String email;
}
