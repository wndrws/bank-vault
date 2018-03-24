package kspt.bank.messaging;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Getter
public abstract class Request {
    private static Long currentId = 0L;

    private final Long id;

    private final Instant timestamp;

    public Request() {
        id = ++currentId;
        timestamp = Instant.now();
    }
}
