package kspt.bank.messaging;

import lombok.Getter;

import java.time.Instant;

@Getter
public abstract class Response {
    private final Long idOfRequest;

    private final Instant timestamp;

    public Response(Long idOfRequest) {
        this.idOfRequest = idOfRequest;
        this.timestamp = Instant.now();
    }
}
