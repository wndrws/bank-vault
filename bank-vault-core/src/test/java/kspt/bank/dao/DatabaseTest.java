package kspt.bank.dao;

import kspt.bank.domain.TestDataGenerator;
import kspt.bank.domain.entities.Cell;
import kspt.bank.domain.entities.CellSize;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.Precious;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseTest {
    private static Connection conn;

    private Savepoint savepoint;

    @BeforeAll
    static void openConnection()
    throws SQLException {
        Cell.AUTOPERSIST = false;
        conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);
    }

    @BeforeEach
    void setUp()
    throws SQLException {
        savepoint = conn.setSavepoint();
    }

    @Test
    void connectionTest()
    throws SQLException {
        final ResultSet rs = conn.createStatement().executeQuery("SELECT VERSION()");
        if (rs.next()) {
            System.out.println(rs.getString(1));
        }
    }

    @Test
    void testCellDataMapper_InsertAndFindUsingCache() {
        // given
        DataMapperRegistry.initialize(conn, true);
        final CellDataMapper mapper = (CellDataMapper) DataMapperRegistry.getMapper(Cell.class);
        final Cell cell = new Cell(123, CellSize.SMALL);
        // when
        mapper.insert(cell);
        final Cell retrievedCell = mapper.find(123);
        // then
        assertThat(retrievedCell).isNotNull();
        assertThat(retrievedCell).isEqualTo(cell);
    }

    @Test
    void testCellDataMapper_InsertAndFindWithoutCache() {
        // given
        DataMapperRegistry.initialize(conn, false);
        final CellDataMapper mapper = (CellDataMapper) DataMapperRegistry.getMapper(Cell.class);
        final Cell cell = new Cell(123, CellSize.SMALL);
        // when
        mapper.insert(cell);
        final Cell retrievedCell = mapper.find(123);
        // then
        assertThat(retrievedCell).isNotNull();
        assertThat(retrievedCell).isNotEqualTo(cell);
        assertThat(retrievedCell).isEqualToComparingFieldByField(retrievedCell);
    }

    @Test
    void testCellDataMapper_Update() {
        // given
        DataMapperRegistry.initialize(conn, false);
        final CellDataMapper mapper = (CellDataMapper) DataMapperRegistry.getMapper(Cell.class);
        // when
        mapper.insert(new Cell(123, CellSize.SMALL));
        final Cell oldCell = mapper.find(123);
        final Cell updatedCell = (Cell) mapper.update(new Cell(123, CellSize.BIG));
        // then
        assertThat(oldCell.getSize()).isEqualTo(CellSize.SMALL);
        assertThat(updatedCell.getSize()).isEqualTo(CellSize.BIG);
    }

    @Test
    void testCellDataMapper_Delete() {
        // given
        DataMapperRegistry.initialize(conn, false);
        final CellDataMapper mapper = (CellDataMapper) DataMapperRegistry.getMapper(Cell.class);
        final Cell cell = new Cell(123, CellSize.SMALL);
        mapper.insert(cell);
        mapper.insert(new Cell(124, CellSize.MEDIUM));
        // when
        mapper.delete(cell);
        final Cell retrievedCellOne = mapper.find(123);
        final Cell retrievedCellTwo = mapper.find(124);
        // then
        assertThat(retrievedCellOne).isNull();
        assertThat(retrievedCellTwo).isNotNull();
    }

    @Test
    void testCellDataMapper_Save() {
        // given
        DataMapperRegistry.initialize(conn, false);
        final CellDataMapper mapper = (CellDataMapper) DataMapperRegistry.getMapper(Cell.class);
        // when
        mapper.save(new Cell(123, CellSize.SMALL));
        final Cell oldCell = mapper.find(123);
        mapper.save(new Cell(123, CellSize.BIG));
        final Cell newCell = mapper.find(123);
        // then
        assertThat(oldCell.getSize()).isEqualTo(CellSize.SMALL);
        assertThat(newCell.getSize()).isEqualTo(CellSize.BIG);
    }

    @Test
    void testCellAndPreciousMappers_PreciousInsertedThroughContainerCell() {
        // given
        DataMapperRegistry.initialize(conn, false);
        final CellDataMapper cellMapper = (CellDataMapper) DataMapperRegistry.getMapper(Cell.class);
        final Cell cell = new Cell(123, CellSize.SMALL);
        final Precious precious = new Precious(1, "Example of precious");
        cell.setContainedPrecious(precious);
        // when
        cellMapper.save(cell);
        final Cell retrievedCell = cellMapper.find(123);
        // then
        assertThat(retrievedCell).isNotNull();
        assertThat(retrievedCell).isEqualToComparingFieldByField(retrievedCell);
        assertThat(retrievedCell.getContainedPrecious()).isEqualToComparingFieldByField(precious);
    }

    @Test
    void testClientMapper_FindByPassportInfo() {
        // given
        DataMapperRegistry.initialize(conn, false);
        final ClientDataMapper clientMapper = (ClientDataMapper) DataMapperRegistry.getMapper(Client.class);
        final Client client = TestDataGenerator.getSampleClient();
        // when
        clientMapper.insert(client);
        final Client foundClient = clientMapper.findByPassportInfo(client.getPassportInfo());
        // then
        assertThat(foundClient).isEqualToComparingFieldByField(client);
    }

    @AfterEach
    void tearDown()
    throws SQLException {
        conn.rollback(savepoint);
    }

    @AfterAll
    static void closeConnection() {
        DatabaseConnection.closeConnection();
        DataMapperRegistry.clear();
    }
}
