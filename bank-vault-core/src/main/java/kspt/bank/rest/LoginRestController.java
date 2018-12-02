package kspt.bank.rest;

import kspt.bank.dto.ClientDTO;
import kspt.bank.recognition.Credentials;
import kspt.bank.services.LoginService;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class LoginRestController {
    @Autowired
    private final LoginService loginService;

    @PostMapping("login")
    public Integer login(@RequestBody final Credentials credentials) {
        return loginService.login(credentials).orElse(-1);
    }

    @PostMapping("register")
    public Integer register(@RequestBody final CredentialsWithClientInto fullInfo) {
        try {
            return loginService.registerUser(fullInfo.userCredentials, fullInfo.clientInfo);
        } catch (IllegalArgumentException ex) {
            return -1;
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
