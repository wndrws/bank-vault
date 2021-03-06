package kspt.bank.rest;

import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.domain.entities.Client;
import kspt.bank.dto.CellRequestDTO;
import kspt.bank.external.Invoice;
import kspt.bank.services.BankVaultFacade;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/apply", consumes = MediaType.APPLICATION_JSON_VALUE)
public class CellApplicationsRestController {
    @Autowired
    private final BankVaultFacade bankVaultFacade;

    @Autowired
    private final ClientsRepository clientsRepository;

    @PostMapping("client/{clientId}")
    public ResponseEntity<Integer> createApplicatioon(@PathVariable("clientId") Integer clientId) {
        final Client client = clientsRepository.find(clientId);
        if (client == null) {
            return ResponseEntity.unprocessableEntity().body(-1);
        } else {
            final Integer createdApplicationId = bankVaultFacade.acceptClientInfo(client);
            return ResponseEntity.ok(createdApplicationId);
        }
    }

    @PostMapping("cell/{appId}")
    public Boolean requestCell(@PathVariable("appId") Integer cellApplicationId,
            @RequestBody CellRequestDTO request) {
        return bankVaultFacade.requestCell(request.size, request.leaseDays, cellApplicationId);
    }

    @PostMapping("payment")
    public void acceptPayment(@RequestBody Invoice invoice) {
        bankVaultFacade.acceptPayment(invoice);
    }
}
