package kspt.bank.rest;

import kspt.bank.dto.ClientDTO;
import kspt.bank.recognition.Credentials;
import kspt.bank.services.LoginService;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
        return loginService.registerUser(fullInfo.userCredentials, fullInfo.clientInfo);
    }

    @Value
    private static class CredentialsWithClientInto {
        Credentials userCredentials;

        ClientDTO clientInfo;
    }
}
