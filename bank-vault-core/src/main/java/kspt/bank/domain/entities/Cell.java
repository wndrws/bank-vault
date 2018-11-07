package kspt.bank.domain.entities;

import kspt.bank.enums.CellSize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(exclude = {"cellLeaseRecord", "containedPrecious"}) // "id",
@ToString(exclude = "cellLeaseRecord")
@Data
@NoArgsConstructor
@Entity
public class Cell {
    @Id
    @GeneratedValue
    private int id;

    @Enumerated(EnumType.STRING)
    @NotNull
    private CellSize size;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "precious_id")
    private Precious containedPrecious;

    @Embedded
    private CellLeaseRecord cellLeaseRecord;

    private boolean pending = false;

    public Cell(final CellSize size) {
        this.size = size;
    }

    public Cell(final CellSize size, final Precious containedPrecious,
            final CellLeaseRecord cellLeaseRecord, final boolean pending) {
        this.size = size;
        this.containedPrecious = containedPrecious;
        this.cellLeaseRecord = cellLeaseRecord;
        this.pending = pending;
    }

    public boolean isEmpty() {
        return containedPrecious == null;
    }

    public void removeContainedPrecious() {
        containedPrecious = null;
    }
}
