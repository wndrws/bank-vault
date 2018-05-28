package kspt.bank.services;

import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.domain.Vault;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CellPendingService {
    @Autowired
    private final ApplicationsRepository applicationsRepository;

    void deletePendingApplications() {
        Vault.getInstance().getPendingCells()
                .forEach(applicationsRepository::deleteApplicationForCell);
    }
}
