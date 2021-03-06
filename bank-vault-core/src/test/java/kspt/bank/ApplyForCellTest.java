package kspt.bank;

import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.config.VaultConfig;
import kspt.bank.domain.*;
import kspt.bank.domain.entities.CellApplication;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.PassportInfo;
import kspt.bank.enums.CellApplicationStatus;
import kspt.bank.enums.CellSize;
import kspt.bank.enums.PaymentMethod;
import kspt.bank.external.Invoice;
import kspt.bank.external.PaymentSystem;
import kspt.bank.external.SimplePaymentSystem;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@AutoConfigurationPackage
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {
        Vault.class, VaultConfig.class, CellApplicationInteractor.class,
        ApplyForCellTest.Config.class })
class ApplyForCellTest {
    @Autowired
    private Vault vault;

    @Autowired
    private ClientsRepository clientsRepository;

    @Autowired
    private ApplicationsRepository applicationsRepository;

    @Autowired
    private PaymentSystem paymentSystem;

    @Autowired
    private CellApplicationInteractor caInteractor;

    @Autowired
    private LeasingController leasingController;

    private final RoleClient roleClient = new RoleClient();

    private final RoleManager roleManager = new RoleManager();

    private CellApplication cellApplication;

    private Invoice invoice;

    @ParameterizedTest
    @ArgumentsSource(LeaseVariantsProvider.class)
    void testBusinessProcess(CellSize cellSize, Integer numOfDays) {
        cellApplication = roleClient.initialApply();
        assertExistenceOfClientAndCellApplication();

        roleClient.requestCell(cellSize, Period.ofDays(numOfDays));
        assertThatRightCellIsReserved(cellSize, Period.ofDays(numOfDays));

        invoice = roleManager.approve();
        assertInvoiceAndApprovalOfCellApplication();

        roleClient.pay(invoice);
        assertThatCellIsLeased();
    }

    private void assertExistenceOfClientAndCellApplication() {
        assertTrue(clientsRepository.containsClientWith(roleClient.passportInfo));
        final Client client = clientsRepository.getClientWith(roleClient.passportInfo);
        assertThat(applicationsRepository.findAllByLeaseholder(client)).contains(cellApplication);
        assertThat(cellApplication.getStatus()).isEqualTo(CellApplicationStatus.CREATED);
    }


    private void assertThatRightCellIsReserved(CellSize size, Period period) {
        assertFalse(vault.isAvailable(cellApplication.getCell()));
        assertFalse(leasingController.isLeased(cellApplication.getCell()));
        assertThat(cellApplication.getCell().getSize()).isEqualTo(size);
        assertThat(cellApplication.getLeasePeriod()).isEqualTo(period);
        assertThat(cellApplication.getStatus()).isEqualTo(CellApplicationStatus.CELL_CHOSEN);
    }

    private void assertInvoiceAndApprovalOfCellApplication() {
        assertFalse(invoice.isPaid());
        assertThat(cellApplication.getStatus()).isEqualTo(CellApplicationStatus.APPROVED);
    }

    private void assertThatCellIsLeased() {
        assertTrue(invoice.isPaid());
        assertTrue(leasingController.isLeased(cellApplication.getCell()));
        assertThat(cellApplication.getStatus()).isEqualTo(CellApplicationStatus.PAID);
    }

    private class RoleClient {
        final PassportInfo passportInfo = TestDataGenerator.getCorrectPassportInfo();

        final String email = "johnwick@example.com";

        final String phone = "+11231237777";

        CellApplication initialApply() {
            return caInteractor.createApplication(passportInfo, phone, email);
        }

        void requestCell(CellSize size, Period period) {
            caInteractor.requestCell(size, period, cellApplication);
        }

        void pay(Invoice invoice) {
            paymentSystem.pay(invoice, invoice.getSum(), PaymentMethod.CASH);
            caInteractor.acceptPayment(invoice);
        }
    }

    private class RoleManager {
        Invoice approve() {
            return caInteractor.approveApplication(cellApplication);
        }
    }

    @Configuration
    static class Config {
        @Bean
        PaymentSystem paymentSystem() {
            return new SimplePaymentSystem();
        }
    }
}
