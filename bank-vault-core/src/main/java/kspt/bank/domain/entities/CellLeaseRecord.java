package kspt.bank.domain.entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.time.LocalDate;

@Embeddable
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class CellLeaseRecord {
    @OneToOne
    @JoinColumn(name = "client_id")
    public Client leaseholder;

    public LocalDate leaseBegin;

    public LocalDate leaseEnd;

    public boolean expired = false;
}
