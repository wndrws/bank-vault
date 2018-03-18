package kspt.bank.domain;

import kspt.bank.domain.ClientPassportValidator.IncorrectPassportInfo;
import org.junit.Test;

import java.time.LocalDate;

public class CellApplicationInteractorTest {
    private final CellApplicationInteractor interactor = new CellApplicationInteractor();

    @Test
    public void testAcceptClientInfo_Correct() {
        final PassportInfo userInfo = new PassportInfo("0409756123", "John", "Wick",
                "", LocalDate.of(1980, 1, 1));
        interactor.acceptClientInfo(userInfo);
    }

    @Test(expected = IncorrectPassportInfo.class)
    public void testAcceptClientInfo_IncorrectSerial() {
        final PassportInfo userInfo = new PassportInfo("123","John", "Wick",
                "", LocalDate.of(1980, 1, 1));
        interactor.acceptClientInfo(userInfo);
    }

    @Test(expected = IncorrectPassportInfo.class)
    public void testAcceptClientInfo_IncorrectFirstName() {
        final PassportInfo userInfo = new PassportInfo("0409756123","", "Wick",
                "", LocalDate.of(1980, 1, 1));
        interactor.acceptClientInfo(userInfo);
    }

    @Test(expected = IncorrectPassportInfo.class)
    public void testAcceptClientInfo_IncorrectLastName() {
        final PassportInfo userInfo = new PassportInfo("0409756123","John", "",
                "", LocalDate.of(1980, 1, 1));
        interactor.acceptClientInfo(userInfo);
    }
}
