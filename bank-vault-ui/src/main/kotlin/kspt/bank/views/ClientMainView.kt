package kspt.bank.views

import javafx.collections.FXCollections
import javafx.scene.control.TableView
import javafx.scene.layout.AnchorPane
import tornadofx.*
import java.time.LocalDate

class ClientMainView: View("Bank Vault") {
    override val root : AnchorPane by fxml("/fxml/ClientMain.fxml")

    private val cellsTable: TableView<CellTableEntry> by fxid("tableMyCells")

    val testData = FXCollections.observableArrayList<CellTableEntry>()

    init {
        cellsTable.readonlyColumn("Идентификатор", CellTableEntry::id)
        cellsTable.readonlyColumn("Статус", CellTableEntry::status)
        cellsTable.readonlyColumn("Размер", CellTableEntry::size)
        cellsTable.readonlyColumn("Содержимое", CellTableEntry::precious)
        cellsTable.readonlyColumn("Аренда до", CellTableEntry::leaseEnding)
        runLater { cellsTable.items = testData }
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
}

class CellTableEntry(val id: Int, val status: String, val size: String, val precious: String,
                     val leaseEnding: String)