package kspt.bank.external;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@EqualsAndHashCode(of = "id")
public class Invoice {
    @NonFinal
    private static long currentId = 0L;

    private long id;

    private long sum;

    @NonFinal
    private boolean isPaid;

    public Invoice(final long sum) {
        this.id = currentId++;
        this.sum = sum;
        this.isPaid = false;
    }

    void markAsPaid() {
        this.isPaid = true;
    }
}
