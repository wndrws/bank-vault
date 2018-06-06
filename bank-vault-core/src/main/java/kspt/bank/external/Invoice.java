package kspt.bank.external;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.io.IOException;

@Value
@EqualsAndHashCode(of = "id")
public class Invoice {
    @NonFinal
    @JsonIgnore
    private static long currentId = 0L;

    private long id;

    private long sum;

    @NonFinal
    private boolean isPaid;

    public Invoice(final long sum) {
        this.id = ++currentId;
        this.sum = sum;
        this.isPaid = false;
    }

    @JsonCreator
    private Invoice(
            @JsonProperty("id") final long id,
            @JsonProperty("sum") final long sum,
            @JsonProperty("paid") final boolean isPaid) {
        this.id = id;
        this.sum = sum;
        this.isPaid = isPaid;
        if (id > currentId) {
            currentId = id;
        }
    }

    void markAsPaid() {
        this.isPaid = true;
    }
}
