package kspt.bank.dao;

import lombok.Getter;

public abstract class ManualIdDomainObject implements DomainObject {
    @Getter
    private final Integer id;

    protected ManualIdDomainObject(final Integer id) {
        this.id = id;
    }
}

