package kspt.bank.services;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.domain.CellApplicationInteractor;
import kspt.bank.domain.CellManipulationInteractor;
import kspt.bank.domain.LeasingController;
import kspt.bank.domain.Vault;
import kspt.bank.domain.entities.*;
import kspt.bank.dto.CellApplicationDTO;
import kspt.bank.dto.CellDTO;
import kspt.bank.dto.ClientDTO;
import kspt.bank.dto.PreciousDTO;
import kspt.bank.enums.CellSize;
import kspt.bank.external.Invoice;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BankVaultFacade {
    @Autowired
    private final CellApplicationInteractor cellApplicationInteractor;

    @Autowired
    private final CellManipulationInteractor cellManipulationInteractor;

    @Autowired
    private final ApplicationsRepository applicationsRepository;

    @Autowired
    private final ClientsRepository clientsRepository;

    @Autowired
    private final Vault vault;

    @Autowired
    private final LeasingController leasingController;

    @Transactional
    public Integer acceptClientInfo(ClientDTO clientInfo) {
        final PassportInfo passportInfo = new PassportInfo(
                clientInfo.passportSerial, clientInfo.firstName, clientInfo.lastName,
                clientInfo.patronymic, clientInfo.birthday);
        final CellApplication cellApplication = cellApplicationInteractor.createApplication(
                passportInfo, clientInfo.phone, clientInfo.email);
        return cellApplication.getId();
    }

    @Transactional
    public Boolean requestCell(CellSize size, Period leasePeriod, Integer cellApplicationId) {
        return cellApplicationInteractor.requestCell(size, leasePeriod,
                cellApplicationInteractor.getApplicationsRepository().find(cellApplicationId));
    }

    public Optional<CellDTO> findCellInfo(Integer cellApplicationId) {
        final CellApplication app = applicationsRepository.find(cellApplicationId);
        if (app == null) return Optional.empty();
        final Cell cell = app.getCell();
        if (cell == null) return Optional.empty();
        return Optional.of(new CellDTO(getCodeName(cell), cell.getSize(), app.getStatus(),
                getLeaseBegin(cell), app.getLeasePeriod().getDays(), getContainedPreciousName(cell),
                app.getId()));
    }

    private static String getContainedPreciousName(final Cell cell) {
        final Precious containedPrecious = cell.getContainedPrecious();
        return containedPrecious == null ? "" : containedPrecious.getName();
    }

    private LocalDate getLeaseBegin(final Cell cell) {
        Preconditions.checkState(vault.isPending(cell) ||
                        leasingController.isLeased(cell),
                "Attempt to getLeaseBegin for not leased nor pending cell!");
        final Range<LocalDate> leasing = leasingController.getInfo(cell);
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

    public List<CellDTO> findCellsInfoByClient(Integer clientId) {
        final List<CellApplication> applications = applicationsRepository.findAll().stream()
                .filter(app -> app.getLeaseholder().getId().equals(clientId))
                .collect(Collectors.toList());
        return applications.stream()
                .map(app -> {
                    final Cell cell = app.getCell();
                    return cell == null ? null : new CellDTO(getCodeName(cell), cell.getSize(),
                            app.getStatus(), getLeaseBegin(cell), app.getLeasePeriod().getDays(),
                            getContainedPreciousName(cell), app.getId());
                }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<CellApplicationDTO> findAllCellApplications() {
        return applicationsRepository.findAll().stream().map(app -> {
            final CellDTO cellInfo = findCellInfo(app.getId()).orElse(null);
            final ClientDTO clientInfo = findClientInfo(app.getLeaseholder().getId())
                    .orElseThrow(() -> new IllegalStateException("Leaseholder is not a client!"));
            return new CellApplicationDTO(app.getId(), cellInfo, clientInfo, app.getLeasePeriod(),
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

    @Transactional
    public void approveApplication(Integer appId) {
        cellApplicationInteractor.approveApplication(applicationsRepository.find(appId));
    }

    @Transactional
    public void declineApplication(Integer appId) {
        applicationsRepository.deleteById(appId);
    }

    @Transactional
    public void acceptPayment(Invoice invoice) {
        cellApplicationInteractor.acceptPayment(invoice);;
    }

    @Transactional
    public void putPrecious(Integer appId, PreciousDTO preciousDTO) {
        final Cell cell = applicationsRepository.find(appId).getCell();
        final Client leaseholder = cell.getCellLeaseRecord().leaseholder;
        cellManipulationInteractor.putPrecious(
                cell, new Precious(preciousDTO.volume, preciousDTO.name), leaseholder);
    }

    @Transactional
    public PreciousDTO getPrecious(Integer appId) {
        final Cell cell = applicationsRepository.find(appId).getCell();
        final Client leaseholder = cell.getCellLeaseRecord().leaseholder;
        final Precious precious = cellManipulationInteractor.getPrecious(cell, leaseholder);
        return new PreciousDTO(precious.getVolume(), precious.getName());
    }
}
