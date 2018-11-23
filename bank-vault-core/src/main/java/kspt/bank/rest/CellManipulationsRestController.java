package kspt.bank.rest;

import kspt.bank.dto.PreciousDTO;
import kspt.bank.services.BankVaultFacade;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "manipulate")
public class CellManipulationsRestController {
    @Autowired
    private final BankVaultFacade bankVaultFacade;

    @PutMapping(value = "put/{appId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void putPrecious(@PathVariable("appId") Integer appId,
            @RequestBody PreciousDTO preciousDTO) {
        bankVaultFacade.putPrecious(appId, preciousDTO);
    }

    @GetMapping(value = "get/{appId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PreciousDTO getPrecious(Integer appId) {
        return bankVaultFacade.getPrecious(appId);
    }
}
