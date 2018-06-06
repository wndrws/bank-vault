package kspt.bank.dao;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public abstract class ManualIdDomainObject implements DomainObject {
    @Getter
    private final Integer id;

    protected ManualIdDomainObject(final Integer id) {
        this.id = id;
    }
}

