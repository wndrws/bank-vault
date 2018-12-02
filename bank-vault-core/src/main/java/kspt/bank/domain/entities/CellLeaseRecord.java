package kspt.bank.domain.entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import java.time.LocalDate;

@Embeddable
@EqualsAndHashCode(exclude = "expired")
@AllArgsConstructor
@NoArgsConstructor
public class CellLeaseRecord {
    @Transient
    @Setter
    private Integer id;

    @OneToOne
    @JoinColumn(name = "client_id")
    public Client leaseholder;

    public LocalDate leaseBegin;

    public LocalDate leaseEnd;

    public Boolean expired = false;
}
