package kspt.bank.dao;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public abstract class AutoIdDomainObject implements DomainObject {
    @Getter
    private final Integer id;

    protected AutoIdDomainObject() {
        id = DataMapperRegistry.getCurrentMaxId(getClass()) + 1;
    }

    protected AutoIdDomainObject(final Integer id) {
        this.id = id;
    }
}
