package kspt.bank.domain.entities;

import kspt.bank.dao.CellApplicationDataMapper;
import kspt.bank.dao.CellDataMapper;
import kspt.bank.dao.DataMapperRegistry;
import kspt.bank.dao.ManualIdDomainObject;
import kspt.bank.domain.LeasingController;
import kspt.bank.domain.Vault;
import kspt.bank.enums.CellSize;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true, exclude = "cellLeaseRecord")
@Data
public class Cell extends ManualIdDomainObject {
    private static boolean AUTOPERSIST = true;

    private final CellSize size;

    private Precious containedPrecious;

    private LeasingController.CellLeaseRecord cellLeaseRecord;

    private boolean pending = false;

    public Cell(final int id, final CellSize size) {
        super(id);
        this.size = size;
        if (AUTOPERSIST) persist();
    }

    public Cell(final int id, final CellSize size, final Precious containedPrecious,
            final LeasingController.CellLeaseRecord cellLeaseRecord, final boolean pending) {
        super(id);
        this.size = size;
        this.containedPrecious = containedPrecious;
        this.cellLeaseRecord = cellLeaseRecord;
        this.pending = pending;
    }

    public void setPending(boolean val) {
        this.pending = val;
        if (AUTOPERSIST) persist();
    }

    public boolean isEmpty() {
        return containedPrecious == null;
    }

    public void setContainedPrecious(final Precious precious) {
        this.containedPrecious = precious;
        if (AUTOPERSIST) persist();
    }

    public void removeContainedPrecious() {
        containedPrecious = null;
        if (AUTOPERSIST) persist();
    }

    public void setCellLeaseRecord(final LeasingController.CellLeaseRecord cellLeaseRecord) {
        this.cellLeaseRecord = cellLeaseRecord;
        if (AUTOPERSIST) persist();
    }

    private void persist() {
        final CellDataMapper mapper = (CellDataMapper) DataMapperRegistry.getMapper(Cell.class);
        if (mapper != null) {
            mapper.save(this);
        }
    }
}
