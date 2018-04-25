package kspt.bank.domain.entities;

import kspt.bank.dao.AutoIdDomainObject;
import kspt.bank.dao.DomainObject;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;

@EqualsAndHashCode(callSuper = true)
@Value
public class Client extends AutoIdDomainObject {

    PassportInfo passportInfo;

    String phone;

    String email;

    public Client(final PassportInfo passportInfo, final String phone, final String email) {
        super();
        this.passportInfo = passportInfo;
        this.phone = phone;
        this.email = email;
    }

    public Client(int id, final PassportInfo passportInfo, final String phone, final String email) {
        super(id);
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
