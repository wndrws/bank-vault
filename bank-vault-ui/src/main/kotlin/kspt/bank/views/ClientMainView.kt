package kspt.bank.views

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.Label
import javafx.scene.control.TableView
import javafx.scene.layout.AnchorPane
import kspt.bank.controllers.CellApplicationController
import kspt.bank.controllers.LoginController
import kspt.bank.controllers.WebTimeController
import tornadofx.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ClientMainView: View("Bank Vault") {
    override val root : AnchorPane by fxml("/fxml/ClientMain.fxml")

    private val cellApplicationController: CellApplicationController by inject()

    private val loginController: LoginController by inject()

    private val timeController: WebTimeController by inject()

    private val timeProperty = SimpleStringProperty()

    private var time by timeProperty

    private val timer: Label by fxid("timer")

    var timerExecutor = Executors.newSingleThreadScheduledExecutor()

    var tableUpdater = Executors.newSingleThreadScheduledExecutor()

    private val cellsTable: TableView<CellTableEntry> by fxid("tableMyCells")

    val cellTableItems = FXCollections.observableArrayList<CellTableEntry>()!!

    init {
        initCellsTable()
        timer.bind(timeProperty)
    }

    private fun initCellsTable() {
        cellsTable.readonlyColumn("Номер", CellTableEntry::id)
        cellsTable.readonlyColumn("Статус", CellTableEntry::status)
        cellsTable.readonlyColumn("Размер", CellTableEntry::size)
        cellsTable.readonlyColumn("Содержимое", CellTableEntry::precious)
        cellsTable.readonlyColumn("Начало аренды", CellTableEntry::leaseBegin)
        cellsTable.readonlyColumn("Аренда (дней)", CellTableEntry::leaseDays)
        cellsTable.items = cellTableItems
    }


    fun lease() {
        this.replaceWith(ClientCellChoiceView::class, sizeToScene = true)
    }

    fun putPrecious() {
        println("PUT")
    }

    fun getPrecious() {
        println("GET")
    }

    fun logout() {
        loginController.logout();
    }

    override fun onDock() {
        super.onDock()
        cellApplicationController.fillCellsTable()
        timerExecutor = Executors.newSingleThreadScheduledExecutor()
        timerExecutor.scheduleAtFixedRate({
            val datetime = timeController.getFormattedDateTime("Europe/Moscow")
            runLater { time = datetime }
        }, 0, 1, TimeUnit.SECONDS)
        tableUpdater = Executors.newSingleThreadScheduledExecutor()
        tableUpdater.scheduleAtFixedRate({
            cellApplicationController.fillCellsTable()
        }, 1, 3, TimeUnit.SECONDS)
    }

    override fun onUndock() {
        super.onUndock()
        timerExecutor.shutdown()
        tableUpdater.shutdown()
        cellTableItems.clear()
    }

    class CellTableEntry(val id: String, val status: String, val size: String, val precious: String,
                         val leaseBegin: String, val leaseDays: Int)
}