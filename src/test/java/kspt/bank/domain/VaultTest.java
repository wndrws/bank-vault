package kspt.bank.domain;

import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellSize;
import kspt.bank.domain.entities.Client;
import org.junit.Test;

import java.util.Optional;

import static kspt.bank.domain.CellApplicationInteractorTest.getSomeCorrectPassportInfo;
import static org.assertj.core.api.Assertions.assertThat;

public class VaultTest {
    @Test
    public void testRequestCellOfSize() {
        final Optional<Cell> cell1 = Vault.getInstance().requestCell(CellSize.MEDIUM);
        cell1.get().setLeaseholder(new Client(1, getSomeCorrectPassportInfo()));
        final Optional<Cell> cell2 = Vault.getInstance().requestCell(CellSize.MEDIUM);
        assertThat(cell1).isNotEqualTo(cell2);
    }
}
