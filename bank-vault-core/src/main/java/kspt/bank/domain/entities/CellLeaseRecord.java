package kspt.bank.domain.entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
    private static int CURRENT_ID = 0;

    @Transient
    private Integer id;

    public CellLeaseRecord(final Client leaseholder, final LocalDate leaseBegin,
            final LocalDate leaseEnd, final Boolean expired) {
        id = CURRENT_ID++;
        this.leaseholder = leaseholder;
        this.leaseBegin = leaseBegin;
        this.leaseEnd = leaseEnd;
        this.expired = expired;
    }

    @OneToOne
    @JoinColumn(name = "client_id")
    public Client leaseholder;

    public LocalDate leaseBegin;

    public LocalDate leaseEnd;

    public Boolean expired = false;
}
