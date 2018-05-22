package kspt.bank.views

import javafx.scene.layout.AnchorPane
import tornadofx.*

class ClientMainView: View("Bank Vault") {
    override val root : AnchorPane by fxml("/fxml/ClientMain.fxml")

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