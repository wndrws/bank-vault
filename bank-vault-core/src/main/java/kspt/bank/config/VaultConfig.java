package kspt.bank.config;

import kspt.bank.boundaries.CellsRepository;
import kspt.bank.domain.LeasingController;
import kspt.bank.domain.VaultHardware;
import kspt.bank.domain.entities.Cell;
import kspt.bank.enums.CellSize;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class VaultConfig {
    @Bean
    public LeasingController leasingController(final Clock clock,
            final CellsRepository cellsRepository) {
        if (cellsRepository.findAllCells().isEmpty()) {
            return new LeasingController(clock, cellsRepository);
        } else {
            return new LeasingController(clock, collectCells(cellsRepository), cellsRepository);
        }
    }

    private EnumMap<CellSize, List<Cell>> collectCells(CellsRepository cellsRepository) {
        return cellsRepository.findAllCells().stream()
                .collect(Collectors.groupingBy(Cell::getSize,
                        () -> new EnumMap<>(CellSize.class), Collectors.toList()));
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public VaultHardware vaultHardware(final CellsRepository cellsRepository) {
        if (cellsRepository.findAllCells().isEmpty()) {
            return new VaultHardware();
        } else {
            return new VaultHardware(collectCells(cellsRepository));
        }
    }
}
