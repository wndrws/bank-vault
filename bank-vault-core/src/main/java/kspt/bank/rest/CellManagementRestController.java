package kspt.bank.rest;

import kspt.bank.services.BankVaultFacade;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/manage")
public class CellManagementRestController {
    @Autowired
    private final BankVaultFacade bankVaultFacade;

    @PatchMapping("approve/{appId}")
    public void approveApplication(@PathVariable("appId") Integer appId) {
        bankVaultFacade.approveApplication(appId);
    }

    @PatchMapping("decline/{appId}")
    public void declineApplication(@PathVariable("appId") Integer appId) {
        bankVaultFacade.declineApplication(appId);
    }
}
