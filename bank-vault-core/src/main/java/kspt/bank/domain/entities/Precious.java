package kspt.bank.domain.entities;

import kspt.bank.dao.AutoIdDomainObject;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = true)
@Value
public class Precious extends AutoIdDomainObject {
    int volume;

    String name;

    public Precious(int volume, String name) {
        super();
        this.volume = volume;
        this.name = name;
    }

    public Precious(int id, int volume, String name) {
        super(id);
        this.volume = volume;
        this.name = name;
    }
}
