package kspt.bank.dao;

import kspt.bank.domain.TestDataGenerator;
import kspt.bank.domain.entities.CellApplication;
import kspt.bank.domain.entities.Client;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class JpaApplicationsRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JpaApplicationsRepository cellApplicationRepository;

    @Test
    void testFind_Existent() {
        // given
        final CellApplication cellApplication = TestDataGenerator.getSampleCellApplication();
        final Client leaseholder = cellApplication.getLeaseholder();
        entityManager.persistAndFlush(leaseholder);
        entityManager.persistAndFlush(cellApplication);
        // when
        final CellApplication foundCellApplication =
                cellApplicationRepository.find(cellApplication.getId());
        // then
        assertThat(foundCellApplication).isEqualTo(cellApplication);
    }

    @Test
    void testFind_NonExistent() {
        // given
        final CellApplication cellApplication = TestDataGenerator.getSampleCellApplication();
        final Client leaseholder = cellApplication.getLeaseholder();
        entityManager.persistAndFlush(leaseholder);
        entityManager.persistAndFlush(cellApplication);
        // when
        final CellApplication foundCellApplication =
                cellApplicationRepository.find(cellApplication.getId() + 1);
        // then
        assertThat(foundCellApplication).isNull();
    }

}
