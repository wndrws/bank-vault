package kspt.bank.domain.entities;

import kspt.bank.dao.CellDataMapper;
import kspt.bank.dao.DataMapperRegistry;
import kspt.bank.dao.ManualIdDomainObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Cell extends ManualIdDomainObject {
    private final CellSize size;

    private Precious containedPrecious;

    public Cell(final int id, final CellSize size) {
        super(id);
        this.size = size;
        persist();
    }

    public Cell(final int id, final CellSize size, final Precious containedPrecious) {
        super(id);
        this.size = size;
        this.containedPrecious = containedPrecious;
    }

    public boolean isEmpty() {
        return containedPrecious == null;
    }

    public void setContainedPrecious(final Precious precious) {
        this.containedPrecious = precious;
        persist();
    }

    public void removeContainedPrecious() {
        containedPrecious = null;
        persist();
    }

    private void persist() {
        final CellDataMapper mapper =
                (CellDataMapper) DataMapperRegistry.getMapper(Cell.class);
        if (mapper != null) {
            mapper.save(this);
        }
    }
}
