package kspt.bank.rest;

import kspt.bank.domain.PutManipulationValidator;
import kspt.bank.dto.PreciousDTO;
import kspt.bank.services.BankVaultFacade;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "manipulate")
public class CellManipulationsRestController {
    @Autowired
    private final BankVaultFacade bankVaultFacade;

    @PutMapping(value = "put/{appId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> putPrecious(@PathVariable("appId") Integer appId,
            @RequestBody PreciousDTO preciousDTO) {
        try {
            bankVaultFacade.putPrecious(appId, preciousDTO);
            return ResponseEntity.ok().build();
        } catch (PutManipulationValidator.ManipulationNotAllowed ex) {
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @GetMapping(value = "get/{appId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PreciousDTO getPrecious(@PathVariable("appId") Integer appId) {
        return bankVaultFacade.getPrecious(appId);
    }
}
