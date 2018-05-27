package kspt.bank.views

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.Label
import javafx.scene.control.TableView
import javafx.scene.layout.AnchorPane
import kspt.bank.controllers.WebTimeController
import kspt.bank.services.BankVaultCoreApplication
import kspt.bank.services.WebTimeService
import tornadofx.*
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ClientMainView: View("Bank Vault") {
    override val root : AnchorPane by fxml("/fxml/ClientMain.fxml")

    private val timeController: WebTimeController by inject()

    private val timeProperty = SimpleStringProperty()

    private var time by timeProperty

    private val timer: Label by fxid("timer")

    private var timerExecutor = Executors.newSingleThreadScheduledExecutor()

    private val cellsTable: TableView<CellTableEntry> by fxid("tableMyCells")

    val cellTableItems = FXCollections.observableArrayList<CellTableEntry>()!!

    init {
        initCellsTable()
        timer.bind(timeProperty)
    }

    private fun initCellsTable() {
        cellsTable.readonlyColumn("Идентификатор", CellTableEntry::id)
        cellsTable.readonlyColumn("Статус", CellTableEntry::status)
        cellsTable.readonlyColumn("Размер", CellTableEntry::size)
        cellsTable.readonlyColumn("Содержимое", CellTableEntry::precious)
        cellsTable.readonlyColumn("Аренда до", CellTableEntry::leaseEnding)
        cellsTable.items = cellTableItems
    }

    private fun displayCurrentTime() {
        time = timeController.getFormattedDateTime("Europe/Moscow")
    }

    fun lease() {
        this.replaceWith(ClientInfoView::class, sizeToScene = true)
    }

    fun putPrecious() {
        println("PUT")
    }

    fun getPrecious() {
        println("GET")
    }

    override fun onDock() {
        super.onDock()
        timerExecutor = Executors.newSingleThreadScheduledExecutor()
        timerExecutor.scheduleAtFixedRate({ runLater { displayCurrentTime() } }, 0, 1, TimeUnit.SECONDS)
    }

    override fun onUndock() {
        super.onUndock()
        timerExecutor.shutdown()
    }
}

class CellTableEntry(val id: Int, val status: String, val size: String, val precious: String,
                     val leaseEnding: String)