package kspt.bank.domain;

import kspt.bank.domain.ClientPassportValidator.IncorrectPassportInfo;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellSize;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.PassportInfo;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ConstantConditions")
public class CellApplicationInteractorTest {
    private final CellApplicationInteractor interactor = new CellApplicationInteractor();

    @Test
    public void testAcceptClientInfo_Correct() {
        interactor.acceptClientInfo(getSomeCorrectPassportInfo());
    }

    static PassportInfo getSomeCorrectPassportInfo() {
        return new PassportInfo("0409756123", "John", "Wick",
                "", LocalDate.of(1980, 1, 1));
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

    @Test
    public void testRequestCellOfSize() {
        // given
        final int cellsCount = Vault.getInstance().getNumberOfAvailableCells(CellSize.MEDIUM);
        // when
        final Optional<Cell> optionalCell = interactor.requestCellOfSize(CellSize.MEDIUM);
        optionalCell.get().setLeaseholder(new Client(1, getSomeCorrectPassportInfo()));
        final int availableCellsCount = Vault.getInstance().getNumberOfAvailableCells(CellSize.MEDIUM);
        // then
        assertThat(availableCellsCount).isEqualTo(cellsCount - 1);
    }
}
