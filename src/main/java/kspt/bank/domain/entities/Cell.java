package kspt.bank.domain.entities;

import lombok.Data;

@Data
public class Cell {
    private final int id;

    private final CellSize size;

    private Precious containedPrecious;

    private Client leaseholder;

    //TODO state related to payment?

    public boolean isEmpty() {
        return containedPrecious == null;
    }

    public void removeContainedPrecious() {
        containedPrecious = null;
    }

    public boolean isNotLeased() {
        return leaseholder == null;
    }
}
