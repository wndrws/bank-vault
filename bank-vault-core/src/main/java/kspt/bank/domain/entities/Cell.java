package kspt.bank.domain.entities;

import kspt.bank.enums.CellSize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@EqualsAndHashCode(exclude = { "dbid","cellLeaseRecord", "containedPrecious"})
@ToString(exclude = "cellLeaseRecord")
@Data
@NoArgsConstructor
public class Cell {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer dbid;

    @NotNull
    @Column(name = "real_id", unique = true)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @NotNull
    private CellSize size;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "precious_id")
    private Precious containedPrecious;

    @Embedded
    private CellLeaseRecord cellLeaseRecord;

    private boolean pending = false;

    public Cell(final int id, final CellSize size) {
        this.id = id;
        this.size = size;
    }

    public Cell(final int id, final CellSize size, final Precious containedPrecious,
            final CellLeaseRecord cellLeaseRecord, final boolean pending) {
        this.id = id;
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
