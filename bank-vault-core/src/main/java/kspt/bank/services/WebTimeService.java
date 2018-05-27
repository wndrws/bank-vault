package kspt.bank.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Service
public class WebTimeService {
    private final RestTemplate restTemplate;

    public final static String ROOT_URL = "http://api.timezonedb.com/v2";

    private final static String QUERY = "key=FH2D5XEBSYPA&format=json&fields=timestamp&by=zone";

    public WebTimeService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.rootUri(ROOT_URL).build();
    }

    @SneakyThrows
    public LocalDateTime getCurrentTime(final ZoneId timeZone) {
        final ResponseEntity<String> response =
                restTemplate.getForEntity(makeTimeQueryForTimeZone(timeZone), String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return extractData(response);
        } else {
            final HttpStatus httpStatus = response.getStatusCode();
            throw new RuntimeException(httpStatus + " " + httpStatus.getReasonPhrase());
        }
    }

    private LocalDateTime extractData(ResponseEntity<String> response)
    throws IOException {
        final JsonNode json = new ObjectMapper().readTree(response.getBody());
        final String apiStatus = json.path("status").asText();
        if (apiStatus.equals("OK")) {
            final Long epochSeconds = json.path("timestamp").asLong();
            return LocalDateTime.ofEpochSecond(epochSeconds, 0, ZoneOffset.UTC);
        } else {
            throw new RuntimeException(json.path("message").asText());
        }
    }

    private static String makeTimeQueryForTimeZone(final ZoneId timeZone) {
        return "/get-time-zone?" + QUERY + "&zone=" + timeZone.toString();
    }
}
