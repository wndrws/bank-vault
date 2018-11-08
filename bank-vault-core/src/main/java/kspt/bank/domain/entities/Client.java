package kspt.bank.domain.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@EqualsAndHashCode
@Data
@Entity
public class Client {
    @Id
    @GeneratedValue
    private int id;

    @Embedded
    PassportInfo passportInfo;

    String phone;

    String email;

    public Client(final PassportInfo passportInfo, final String phone, final String email) {
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
