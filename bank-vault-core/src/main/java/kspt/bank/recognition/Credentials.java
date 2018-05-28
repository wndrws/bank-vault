package kspt.bank.recognition;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.io.Serializable;

@Value
public class Credentials implements Serializable {
    @JsonProperty
    private final String login;

    @JsonProperty
    private final String password;
}
