package kspt.bank.domain.entities;

import lombok.Value;
import lombok.experimental.NonFinal;

@Value
public class Client {
    @NonFinal
    private static int currentId;

    int id;

    PassportInfo passportInfo;

    String phone;

    String email;

    public Client(final PassportInfo passportInfo, final String phone, final String email) {
        this.id = currentId++;
        this.passportInfo = passportInfo;
        this.phone = phone;
        this.email = email;
    }

    public boolean equalsIgnoringId(final Client other) {
        return passportInfo.equals(other.passportInfo)
                && phone.equals(other.phone)
                && email.equals(other.email);
    }
}
