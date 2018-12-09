package kspt.bank.rest;

import kspt.bank.domain.ClientPassportValidator;
import kspt.bank.dto.ClientDTO;
import kspt.bank.recognition.Credentials;
import kspt.bank.services.LoginService;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class LoginRestController {
    @Autowired
    private final LoginService loginService;

    @PostMapping(value = "login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Integer login(@RequestBody final Credentials credentials) {
        return loginService.login(credentials).orElse(-1);
    }

    @PostMapping(value = "register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> register(@RequestBody final CredentialsWithClientInto fullInfo) {
        try {
            final int id = loginService.registerUser(fullInfo.userCredentials, fullInfo.clientInfo);
            return ResponseEntity.ok(id);
        } catch (IllegalArgumentException __) {
            return ResponseEntity.badRequest().body(-1);
        } catch (ClientPassportValidator.IncorrectPassportInfo __) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(-1);
        }
    }

    @GetMapping("isRegistered")
    public Boolean isRegistered(@RequestParam("username") final String username) {
        return loginService.isRegistered(username);
    }

    @Value
    private static class CredentialsWithClientInto {
        Credentials userCredentials;

        ClientDTO clientInfo;
    }
}
