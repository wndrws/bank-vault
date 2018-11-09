package kspt.bank.domain;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import kspt.bank.TestConfig;
import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.boundaries.CellsRepository;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.config.VaultConfig;
import kspt.bank.domain.ClientPassportValidator.IncorrectPassportInfo;
import kspt.bank.domain.entities.CellApplication;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.PassportInfo;
import kspt.bank.enums.CellApplicationStatus;
import kspt.bank.enums.CellSize;
import kspt.bank.enums.PaymentMethod;
import kspt.bank.external.Invoice;
import kspt.bank.external.PaymentSystem;
import kspt.bank.external.SimplePaymentSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Clock;
import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {
        CellApplicationInteractor.class, Vault.class, VaultConfig.class, TestConfig.class,
        CellApplicationInteractorTest.Config.class })
class CellApplicationInteractorTest {
    private static final BiMap<Invoice, Integer> INVOICE_MAP = HashBiMap.create();

    @Autowired
    private ClientsRepository clientsRepository;

    @Autowired
    private ApplicationsRepository applicationsRepository;

    @Autowired
    private LeasingController leasingController;

    @Autowired
    private PaymentSystem paymentSystem;

    @Autowired
    private CellsRepository cellsRepository;

    @Autowired
    private CellApplicationInteractor interactor;

    @AfterEach
    void clearMocks() {
        Mockito.clearInvocations(clientsRepository);
        Mockito.clearInvocations(applicationsRepository);
        INVOICE_MAP.clear();
    }

    @Test
    void testCreateApplication_NewClient() {
        // given
        final Client client = TestDataGenerator.getSampleClient();
        when(clientsRepository.containsClientWith(client.getPassportInfo())).thenReturn(false);
        // when
        final CellApplication application = interactor.createApplication(
                client.getPassportInfo(), client.getPhone(), client.getEmail());
        // then
        assertThat(application.getLeaseholder()).isEqualToIgnoringGivenFields(client, "id");
        assertThat(application.getStatus()).isEqualTo(CellApplicationStatus.CREATED);
        verify(applicationsRepository).save(application);
        verify(clientsRepository).add(argThat(c -> c.equalsIgnoringId(client)));
    }

    @Test
    void testCreateApplication_ExistingClient() {
        // given
        final Client client = TestDataGenerator.getSampleClient();
        final PassportInfo passportInfo = client.getPassportInfo();
        when(clientsRepository.containsClientWith(passportInfo)).thenReturn(true);
        when(clientsRepository.getClientWith(passportInfo)).thenReturn(client);
        // when
        final CellApplication application = interactor.createApplication(
                client.getPassportInfo(), client.getPhone(), client.getEmail());
        // then
        assertThat(application.getLeaseholder()).isEqualTo(client);
        assertThat(application.getStatus()).isEqualTo(CellApplicationStatus.CREATED);
        verify(applicationsRepository).save(application);
        verify(clientsRepository, never()).add(any());
    }

    @Test
    void testCreateApplication_IncorrectSerial() {
        // given
        final PassportInfo userInfo = TestDataGenerator.getPassportInfoWithIncorrectSerial();
        // then
        assertThrows(IncorrectPassportInfo.class, // when
                () -> interactor.createApplication(userInfo, "", ""));
    }

    @Test
    void testCreateApplication_IncorrectFirstName() {
        // given
        final PassportInfo userInfo = TestDataGenerator.getPassportInfoWithIncorrectFirstName();
        // then
        assertThrows(IncorrectPassportInfo.class, // when
                () -> interactor.createApplication(userInfo, "", ""));
    }

    @Test
    void testCreateApplication_IncorrectLastName() {
        // given
        final PassportInfo userInfo = TestDataGenerator.getPassportInfoWithIncorrectLastName();
        // then
        assertThrows(IncorrectPassportInfo.class, // when
                () -> interactor.createApplication(userInfo, "", ""));
    }

    @ParameterizedTest
    @ArgumentsSource(CellSizesWithTotalsProvider.class)
    void testRequestCellOfSize(CellSize size, int totalCellsOfThatSize) {
        // given
        Assumptions.assumeTrue(totalCellsOfThatSize > 0);
        final Period leasePeriod = Period.ofMonths(1);
        final CellApplication cellApplication = TestDataGenerator.getSampleCellApplication();
        // when
        final boolean success = interactor.requestCell(size, leasePeriod, cellApplication);
        // then
        assertThat(success).isTrue();
        assertThat(cellApplication.getCell()).isNotNull();
        assertThat(cellApplication.getLeasePeriod()).isEqualTo(leasePeriod);
        assertThat(cellApplication.getStatus()).isEqualTo(CellApplicationStatus.CELL_CHOSEN);
    }
//
//    private CellApplication saveCellApplication(final CellApplication cellApplication) {
//        cellsRepository.saveCell(cellApplication.getCell());
//        clientsRepository.add(cellApplication.getLeaseholder());
//        applicationsRepository.saveCell(cellApplication);
//        return cellApplication;
//    }

    @Test
    void testApproveApplication() {
        // given
        final CellApplication cellApplication =
                TestDataGenerator.getCellApplication(CellApplicationStatus.CELL_CHOSEN);
        cellApplication.setId(123);
        // when
        final Invoice invoice = interactor.approveApplication(cellApplication);
        // then
        assertThat(invoice.isPaid()).isFalse();
        assertThat(invoice.getSum()).isEqualTo(cellApplication.calculateLeaseCost());
        assertThat(INVOICE_MAP.get(invoice)).isEqualTo(cellApplication.getId());
        assertThat(cellApplication.getStatus()).isEqualTo(CellApplicationStatus.APPROVED);
    }

    @ParameterizedTest
    @EnumSource(PaymentMethod.class)
    void testAcceptPayment(PaymentMethod paymentMethod) {
        // given
        final CellApplication cellApplication =
                TestDataGenerator.getCellApplication(CellApplicationStatus.APPROVED);
        cellsRepository.saveCell(cellApplication.getCell());
        final Invoice invoice = new Invoice(cellApplication.calculateLeaseCost());
        INVOICE_MAP.put(invoice, cellApplication.getId());
        when(applicationsRepository.find(cellApplication.getId())).thenReturn(cellApplication);
        paymentSystem.pay(invoice, invoice.getSum(), paymentMethod);
        // when
        interactor.acceptPayment(invoice);
        // then
        assertThat(cellApplication.getStatus()).isEqualTo(CellApplicationStatus.PAID);
        assertTrue(leasingController.isLeased(cellApplication.getCell()));
    }

    @Configuration
    static class Config {
        @Bean
        LeasingController leasingController(final Clock clock, final CellsRepository cellsRepository) {
            return new LeasingController(clock, cellsRepository);
        }

        @Bean
        @Primary
        PaymentSystem paymentSystem() {
            return new SimplePaymentSystem(INVOICE_MAP);
        }

        @Bean
        @Primary
        ClientsRepository clientsRepository() {
            return mock(ClientsRepository.class);
        }

        @Bean
        @Primary
        ApplicationsRepository applicationsRepository() {
            return mock(ApplicationsRepository.class);
        }
    }
}
