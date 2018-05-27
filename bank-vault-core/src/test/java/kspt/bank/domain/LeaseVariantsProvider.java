package kspt.bank.domain;

import kspt.bank.enums.CellSize;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.Arrays;
import java.util.stream.Stream;

public class LeaseVariantsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
        return Arrays.stream(CellSize.values()).flatMap(size -> Stream.of(
                Arguments.of(size, 1), Arguments.of(size, 2), Arguments.of(size, 3)
        ));
    }
}