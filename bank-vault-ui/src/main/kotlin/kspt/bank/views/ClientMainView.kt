package kspt.bank.views

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.Label
import javafx.scene.control.TableView
import javafx.scene.layout.AnchorPane
import tornadofx.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ClientMainView: View("Bank Vault") {
    override val root : AnchorPane by fxml("/fxml/ClientMain.fxml")

    private val timeProperty = SimpleStringProperty()

    private var time by timeProperty

    private val timer: Label by fxid("timer")

    private val timeAPI: Rest by inject()

    private val timeAPIQueryParams = mapOf(
            "key" to "FH2D5XEBSYPA",
            "format" to "json",
            "fields" to "formatted",
            "by" to "zone",
            "zone" to "Europe/Moscow"
    )

    private val timerExecutor = Executors.newSingleThreadScheduledExecutor()

    private val cellsTable: TableView<CellTableEntry> by fxid("tableMyCells")

    val testData = FXCollections.observableArrayList<CellTableEntry>()

    init {
        initCellsTable()
        initTimeRestClient()
    }

    private fun initCellsTable() {
        cellsTable.readonlyColumn("Идентификатор", CellTableEntry::id)
        cellsTable.readonlyColumn("Статус", CellTableEntry::status)
        cellsTable.readonlyColumn("Размер", CellTableEntry::size)
        cellsTable.readonlyColumn("Содержимое", CellTableEntry::precious)
        cellsTable.readonlyColumn("Аренда до", CellTableEntry::leaseEnding)
        cellsTable.items = testData
    }

    private fun initTimeRestClient() {
        timer.bind(timeProperty)
        timeAPI.baseURI = "http://api.timezonedb.com/v2"
    }

    private fun displayCurrentTime() {
        val response = timeAPI.get("get-time-zone${timeAPIQueryParams.queryString}")
        try {
            println(response.request.uri)
            println(response.statusCode)
            time = if (response.ok()) {
                response.one().getString("formatted")
            } else {
                "<error reading time>"
            }
        } finally {
            response.consume()
        }
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
        timerExecutor.scheduleAtFixedRate({ runLater { displayCurrentTime() } }, 0, 1, TimeUnit.SECONDS)
    }

    override fun onUndock() {
        super.onUndock()
        timerExecutor.shutdown()
    }
}

class CellTableEntry(val id: Int, val status: String, val size: String, val precious: String,
                     val leaseEnding: String)