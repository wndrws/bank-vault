package kspt.bank.recognition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserStorage implements Serializable {
    @JsonProperty("id")
    private HashMap<String, Integer> usernameToUserId = new HashMap<>();

    @JsonProperty("pass")
    private HashMap<String, Integer> usernameToPasswordHash = new HashMap<>();

    private static final ObjectMapper mapper = new ObjectMapper();

    private final File file = new File("users.json");

    static {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.enableDefaultTyping();
    }

    public void createUser(Credentials credentials, Integer id) {
        usernameToUserId.put(credentials.getLogin(), id);
        usernameToPasswordHash.put(credentials.getLogin(), credentials.getPassword().hashCode());
    }

    public Integer findUser(Credentials credentials) {
        final Integer passwordHash = usernameToPasswordHash.get(credentials.getLogin());
        if (passwordHash != null && passwordHash.equals(credentials.getPassword().hashCode())) {
            return usernameToUserId.get(credentials.getLogin());
        } else {
            return null;
        }
    }

    @PostConstruct
    private void load() {
        try {
            final JsonNode root = mapper.readTree(file);
            if (root != null) {
                usernameToUserId =
                        mapper.treeToValue(root.path("id"), usernameToUserId.getClass());
                usernameToPasswordHash =
                        mapper.treeToValue(root.path("pass"), usernameToPasswordHash.getClass());
            }
        } catch (IOException e) {
            log.error("Could not load users credentials: {}", e.getMessage());
        }
    }

    @PreDestroy
    private void save() {
        try {
            mapper.writeValue(file, this);
        } catch (IOException e) {
            log.error("Could not save users credentials: ", e.getMessage());
        }
    }
}
