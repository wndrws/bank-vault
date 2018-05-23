package kspt.bank.external;

import kspt.bank.domain.entities.PassportInfo;

import java.util.Optional;

public interface CreditHistoryAPI {
    Optional<CreditHistory> getCreditHistoryOfPerson(PassportInfo passportInfo);
}
