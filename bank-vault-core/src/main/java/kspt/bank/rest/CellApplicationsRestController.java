package kspt.bank.rest;

import kspt.bank.dto.CellRequestDTO;
import kspt.bank.dto.ClientDTO;
import kspt.bank.external.Invoice;
import kspt.bank.services.BankVaultFacade;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "apply", consumes = MediaType.APPLICATION_JSON_VALUE)
public class CellApplicationsRestController {
    @Autowired
    private final BankVaultFacade bankVaultFacade;

    @PostMapping("client")
    public Integer acceptClientInfo(@RequestBody ClientDTO clientInfo) {
        return bankVaultFacade.acceptClientInfo(clientInfo);
    }

    @PostMapping("cell/{appId}")
    public Boolean requestCell(@PathVariable("appId") Integer cellApplicationId,
            @RequestBody CellRequestDTO request) {
        return bankVaultFacade.requestCell(request.size, request.leasePeriod, cellApplicationId);
    }

    @PostMapping("payment")
    public void acceptPayment(@RequestBody Invoice invoice) {
        bankVaultFacade.acceptPayment(invoice);
    }
}
