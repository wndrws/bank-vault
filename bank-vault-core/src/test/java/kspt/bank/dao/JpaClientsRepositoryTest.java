package kspt.bank.dao;

import kspt.bank.domain.TestDataGenerator;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.PassportInfo;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class JpaClientsRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JpaClientsRepository clientsRepository;

    @Test
    void testAdd() {
       // given
        final Client client = TestDataGenerator.getSampleClient();
        Assumptions.assumeTrue(client.getId() == null);
        // when
        clientsRepository.add(client);
        // then
        final List<Client> clients = clientsRepository.findAll();
        assertThat(clients).hasSize(1);
        assertThat(client.getId()).isNotNull();
        assertThat(clients.get(0)).isEqualTo(client);
    }


    @Test
    void testFind_Existent() {
        // given
        final Client client = entityManager.persistAndFlush(TestDataGenerator.getSampleClient());
        // when
        final Client foundClient = clientsRepository.find(client.getId());
        // then
        assertThat(foundClient).isEqualTo(client);
    }

    @Test
    void testFind_NonExistent() {
        // given
        final Client client = entityManager.persistAndFlush(TestDataGenerator.getSampleClient());
        // when
        final Client foundClient = clientsRepository.find(client.getId() + 1);
        // then
        assertThat(foundClient).isNull();
    }

    @Test
    void testContainsClientWith_Existent() {
        // given
        final Client client = entityManager.persistAndFlush(TestDataGenerator.getSampleClient());
        final PassportInfo info = client.getPassportInfo();
        // when
        final boolean contains = clientsRepository.containsClientWith(info);
        // then
        assertThat(contains).isTrue();
    }

    @Test
    void testContainsClientWith_NonExistent() {
        // given
        entityManager.persistAndFlush(TestDataGenerator.getSampleClient());
        final PassportInfo info = TestDataGenerator.getPassportInfoWithIncorrectSerial();
        // when
        final boolean contains = clientsRepository.containsClientWith(info);
        // then
        assertThat(contains).isFalse();
    }


    @Test
    void testGetClientWith_Existent() {
        // given
        final Client client = entityManager.persistAndFlush(TestDataGenerator.getSampleClient());
        final PassportInfo info = client.getPassportInfo();
        // when
        final Client foundClient = clientsRepository.getClientWith(info);
        // then
        assertThat(foundClient).isEqualTo(client);
    }

    @Test
    void testGetClientWith_NonExistent() {
        // given
        entityManager.persistAndFlush(TestDataGenerator.getSampleClient());
        final PassportInfo info = TestDataGenerator.getPassportInfoWithIncorrectSerial();
        // when
        final Client foundClient = clientsRepository.getClientWith(info);
        // then
        assertThat(foundClient).isNull();
    }
}
