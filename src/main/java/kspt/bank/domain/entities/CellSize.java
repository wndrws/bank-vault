package kspt.bank.domain.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CellSize {
    SMALL(1), MEDIUM(2), BIG(4);

    @Getter
    final int volume;
}
