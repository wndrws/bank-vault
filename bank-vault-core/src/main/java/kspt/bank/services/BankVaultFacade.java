package kspt.bank.services;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.domain.CellApplicationInteractor;
import kspt.bank.domain.Vault;
import kspt.bank.domain.entities.*;
import kspt.bank.dto.CellApplicationDTO;
import kspt.bank.dto.CellDTO;
import kspt.bank.dto.ClientDTO;
import kspt.bank.enums.CellSize;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BankVaultFacade {
    @Autowired
    private final CellApplicationInteractor cellApplicationInteractor;

    @Autowired
    private final ApplicationsRepository applicationsRepository;

    @Autowired
    private final ClientsRepository clientsRepository;

    @Autowired
    private final TransactionManager transactionManager;

    public Integer acceptClientInfo(ClientDTO clientInfo) {
        final PassportInfo passportInfo = new PassportInfo(
                clientInfo.passportSerial, clientInfo.firstName, clientInfo.lastName,
                clientInfo.patronymic, clientInfo.birthday);
        final CellApplication cellApplication = transactionManager.runTransactional(() ->
                cellApplicationInteractor.createApplication(
                        passportInfo, clientInfo.phone, clientInfo.email));
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

//    public List<CellDTO> findCellsInfoByClient(Integer clientId) {
//        final CellApplication app = applicationsRepository.find(cellApplicationId);
//        final Cell cell = app.getCell();
//        if (cell == null) {
//            return Optional.empty();
//        } else {
//            return Optional.of(new CellDTO(getCodeName(cell), cell.getSize(), app.getStatus(),
//                    getLeaseBegin(cell), app.getLeasePeriod(), getContainedPreciousName(cell)));
//        }
//    }

    public List<CellApplicationDTO> findAllCellApplications() {
        return applicationsRepository.findAll().stream().map(app -> {
            final CellDTO cellInfo = findCellInfo(app.getId()).orElse(null);
            final ClientDTO clientInfo = findClientInfo(app.getLeaseholder().getId())
                    .orElseThrow(() -> new IllegalStateException("Leaseholder is not a client!"));
            return new CellApplicationDTO(cellInfo, clientInfo, app.getLeasePeriod(),
                    app.getStatus(), app.calculateLeaseCost());
        }).collect(Collectors.toList());
    }

    public Optional<ClientDTO> findClientInfo(Integer id) {
        final Client client = clientsRepository.find(id);
        if (client == null) {
            return Optional.empty();
        } else {
            final PassportInfo passportInfo = client.getPassportInfo();
            return Optional.of(new ClientDTO(passportInfo.getSerial(), passportInfo.getFirstName(),
                    passportInfo.getLastName(), passportInfo.getPatronymic(),
                    passportInfo.getBirthDate(), client.getEmail(), client.getPhone()));
        }
    }
}