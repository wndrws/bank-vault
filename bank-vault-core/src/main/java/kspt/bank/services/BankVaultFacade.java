package kspt.bank.services;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.domain.CellApplicationInteractor;
import kspt.bank.domain.Vault;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellApplication;
import kspt.bank.domain.entities.Precious;
import kspt.bank.dto.CellDTO;
import kspt.bank.enums.CellSize;
import kspt.bank.domain.entities.PassportInfo;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BankVaultFacade {
    @Autowired
    private final CellApplicationInteractor cellApplicationInteractor;

    @Autowired
    private final ApplicationsRepository applicationsRepository;

    @Autowired
    private final TransactionManager transactionManager;

    public Integer acceptClientInfo(String serial, String firstName, String lastName,
            String patronymic, LocalDate birthday, String phone, String email) {
        final PassportInfo passportInfo = new PassportInfo(
                serial, firstName, lastName, patronymic, birthday);
        final CellApplication cellApplication = transactionManager.runTransactional(() ->
                cellApplicationInteractor.createApplication(passportInfo, phone, email));
        return cellApplication.getId();
    }

    public Boolean requestCell(CellSize size, Period leasePeriod, Integer cellApplicationId) {
        return transactionManager.runTransactional(() ->
                cellApplicationInteractor.requestCell(size, leasePeriod,
                cellApplicationInteractor.getApplicationsRepository().find(cellApplicationId)));
    }

    public Optional<CellDTO> findCellInfo(Integer cellApplicationId) {
        final CellApplication app = applicationsRepository.find(cellApplicationId);
        final Cell cell = app.getCell();
        if (cell == null) {
            return Optional.empty();
        } else {
            return Optional.of(new CellDTO(getCodeName(cell), cell.getSize(), app.getStatus(),
                    getLeaseBegin(cell), app.getLeasePeriod(), getContainedPreciousName(cell)));
        }
    }

    private static String getContainedPreciousName(final Cell cell) {
        final Precious containedPrecious = cell.getContainedPrecious();
        return containedPrecious == null ? "" : containedPrecious.getName();
    }

    private static LocalDate getLeaseBegin(final Cell cell) {
        Preconditions.checkState(Vault.getInstance().isPending(cell) ||
                        Vault.getInstance().getLeasingController().isLeased(cell),
                "Attempt to getLeaseBegin for not leased nor pending cell!");
        final Range<LocalDate> leasing = Vault.getInstance().getLeasingController().getInfo(cell);
        return leasing == null ? null : leasing.lowerEndpoint();
    }

    private static String getCodeName(final Cell cell) {
        String prefix = "";
        switch (cell.getSize()) {
            case SMALL: prefix = "S"; break;
            case MEDIUM: prefix = "M"; break;
            case BIG: prefix = "B"; break;
        }
        return String.format("%s%03d", prefix, cell.getId());
    }
}
