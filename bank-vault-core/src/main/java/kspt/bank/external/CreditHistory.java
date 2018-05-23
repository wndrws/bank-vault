package kspt.bank.external;

import kspt.bank.domain.entities.PassportInfo;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CreditHistory {
    private final PassportInfo infoAboutSubject;

    private final List<CreditHistoryEntry> entryList = new ArrayList<>();
}
