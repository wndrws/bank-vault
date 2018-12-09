package kspt.bank.rest;

import kspt.bank.dto.CellApplicationDTO;
import kspt.bank.dto.CellDTO;
import kspt.bank.services.BankVaultFacade;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/info", produces = MediaType.APPLICATION_JSON_VALUE)
public class CellInformationRestController {
    @Autowired
    private final BankVaultFacade bankVaultFacade;

    @GetMapping("cell/{appId}")
    public ResponseEntity<CellDTO> findCellInfo(@PathVariable("appId") Integer cellApplicationId) {
        return bankVaultFacade.findCellInfo(cellApplicationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("cellsByClient/{id}")
    public List<CellDTO> findCellsInfoByClient(@PathVariable("id") Integer clientId) {
        return bankVaultFacade.findCellsInfoByClient(clientId);
    }

    @GetMapping("applications")
    public List<CellApplicationDTO> findAllCellApplications() {
        return bankVaultFacade.findAllCellApplications();
    }
}
