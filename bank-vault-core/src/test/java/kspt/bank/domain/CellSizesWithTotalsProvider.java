package kspt.bank.domain;

import kspt.bank.domain.entities.CellSize;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

public class CellSizesWithTotalsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
                Arguments.of(CellSize.SMALL, VaultHardware.NUMBER_OF_SMALL_CELLS),
                Arguments.of(CellSize.MEDIUM, VaultHardware.NUMBER_OF_MEDIUM_CELLS),
                Arguments.of(CellSize.BIG, VaultHardware.NUMBER_OF_BIG_CELLS)
        );
    }
}
