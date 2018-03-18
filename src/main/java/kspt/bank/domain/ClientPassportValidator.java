package kspt.bank.domain;

import kspt.bank.domain.entities.PassportInfo;

public class ClientPassportValidator {
    private static final int DIGITS_COUNT_IN_SERIAL = 10;

    public static void checkValidity(PassportInfo info)
    throws IncorrectPassportInfo {
        checkSerial(info);
        checkFirstName(info);
        checkLastName(info);
    }

    private static void checkSerial(PassportInfo info) {
        if (info.getSerial().length() != DIGITS_COUNT_IN_SERIAL) {
            throw new IncorrectPassportInfo("Serial is incorrect!");
        }
    }

    private static void checkFirstName(PassportInfo info) {
        checkNotBlank(info.getFirstName(), "First Name field");
    }

    private static void checkLastName(PassportInfo info) {
        checkNotBlank(info.getLastName(), "Last Name field");
    }

    private static void checkNotBlank(String str, String fieldName) {
        if (str.isEmpty()) {
            throw new IncorrectPassportInfo(fieldName + "is blank!");
        }
    }

    static class IncorrectPassportInfo extends RuntimeException {
        IncorrectPassportInfo(String msg) {
            super(msg);
        }
    }
}
