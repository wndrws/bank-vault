package kspt.bank.dao;

import lombok.Getter;

public abstract class AutoIdDomainObject implements DomainObject {
    private static Integer currentId = 0;

    @Getter
    private final Integer id;

    protected AutoIdDomainObject() {
        id = ++currentId;
    }

    protected AutoIdDomainObject(final Integer id) {
        this.id = id;
    }
}
