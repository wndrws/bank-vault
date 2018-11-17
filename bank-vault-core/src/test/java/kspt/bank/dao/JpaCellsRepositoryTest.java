package kspt.bank.dao;

import kspt.bank.domain.entities.Cell;
import kspt.bank.enums.CellSize;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class JpaCellsRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JpaCellsRepository cellsRepository;

    @ParameterizedTest
    @EnumSource(CellSize.class)
    void testSave(final CellSize size) {
        // given
        final Cell cell = new Cell(1, size);
        // when
        final Cell savedCell = cellsRepository.saveCell(cell);
        final Cell sameCell = entityManager.find(Cell.class, savedCell.getDbid());
        // then
        assertThat(savedCell).isEqualTo(sameCell);
        assertThat(savedCell.getId()).isEqualTo(cell.getId());
        assertThat(savedCell.getDbid()).isNotNull();
        assertThat(savedCell.getSize()).isEqualTo(size);
        assertThat(savedCell.isPending()).isEqualTo(false);
        assertThat(savedCell.isEmpty()).isEqualTo(true);
        assertThat(savedCell.getCellLeaseRecord()).isNull();
        assertThat(savedCell.getContainedPrecious()).isNull();
    }

    @Test
    void testFindCell_Existent() {
        // given
        final Cell cell = entityManager.persistAndFlush(new Cell(10, CellSize.MEDIUM));
        // when
        final Cell foundCell = cellsRepository.findCell(cell.getId());
        // then
        assertThat(foundCell).isEqualTo(cell);
    }

    @Test
    void testFindCell_NonExistent() {
        // given
        final Cell cell = entityManager.persistAndFlush(new Cell(1, CellSize.MEDIUM));
        // when
        final Cell foundCell = cellsRepository.findCell(cell.getId() + 1);
        // then
        assertThat(foundCell).isNull();
    }

    @Test
    void testFindAllCells() {
        // given
        final Cell cell1 = entityManager.persist(new Cell(1, CellSize.SMALL));
        final Cell cell2 = entityManager.persist(new Cell(2, CellSize.MEDIUM));
        final Cell cell3 = entityManager.persist(new Cell(3, CellSize.BIG));
        // when
        final List<Cell> foundCells = cellsRepository.findAllCells();
        // then
        assertThat(foundCells).containsExactlyInAnyOrder(cell1, cell2, cell3);
    }

    @Test
    void testIsPending() {
        // given
        final Cell cellOne = entityManager.persistAndFlush(new Cell(1, CellSize.MEDIUM));
        final Cell cellTwo = entityManager.persistAndFlush(new Cell(2, CellSize.BIG, null, null, true));
        // when
        final boolean cellOnePending = cellsRepository.isPending(cellOne);
        final boolean cellTwoPending = cellsRepository.isPending(cellTwo);
        // then
        assertThat(cellOnePending).isFalse();
        assertThat(cellTwoPending).isTrue();
    }

    @Test
    void testFindAllPendingCells() {
        // given
        final Cell cell1 = entityManager.persistAndFlush(new Cell(1, CellSize.BIG));
        final Cell cell2 = entityManager.persistAndFlush(new Cell(2, CellSize.BIG, null, null, true));
        final Cell cell3 = entityManager.persistAndFlush(new Cell(3, CellSize.BIG, null, null, true));
        // when
        final List<Cell> allPendingCells = cellsRepository.findAllPendingCells();
        // then
        assertThat(allPendingCells).containsExactlyInAnyOrder(cell2, cell3);
    }

}
