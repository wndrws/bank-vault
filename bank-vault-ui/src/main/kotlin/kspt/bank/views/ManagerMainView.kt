package kspt.bank.views

import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.control.ListView
import javafx.scene.layout.AnchorPane
import kspt.bank.controllers.CellApplicationController
import kspt.bank.dto.CellApplicationDTO
import tornadofx.*

class ManagerMainView : View() {
    override val root : AnchorPane by fxml("/fxml/ManagerMain.fxml")

    private val cellApplicationController: CellApplicationController by inject()

    private val cellApplicationList: ListView<CellApplicationListEntry> by fxid("listCellApplications")

    val cellApplicationListItems = FXCollections.observableArrayList<CellApplicationListEntry>()!!

    init {
        cellApplicationList.cellFormat {
            val cellCode = this.item.cellCode
            val clientName = this.item.client
            vbox {
                spacing = 10.0
                padding = Insets(10.0)
                label(cellCode)
                label(clientName)
            }
        }
        cellApplicationList.items = cellApplicationListItems
    }

    override fun onDock() {
        super.onDock()
        cellApplicationController.fillCellApplicationList()
    }

    class CellApplicationListEntry(val cellCode: String, val client: String, val info: CellApplicationDTO)
}