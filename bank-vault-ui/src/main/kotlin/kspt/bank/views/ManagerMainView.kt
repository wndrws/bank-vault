package kspt.bank.views

import javafx.scene.control.ListView
import javafx.scene.layout.AnchorPane
import tornadofx.*

class ManagerMainView : View() {
    override val root : AnchorPane by fxml("/fxml//ManagerMain.fxml")

    private val cellApplicationList: ListView<CellApplicationListEntry> by fxid("listCellApplications")

    class CellApplicationListEntry(val id: Int, val datetime: String, val client: String)
}