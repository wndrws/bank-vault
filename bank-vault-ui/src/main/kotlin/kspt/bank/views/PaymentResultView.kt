package kspt.bank.views

import javafx.geometry.Insets
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import tornadofx.Fragment

class PaymentResultView: Fragment() {
    val message: String by param()

    val text: Text by fxid("textMessage")

    override val root: VBox by fxml("/fxml/PaymentNotification.fxml")

    init {
        text.text = message
        root.padding = Insets(15.0)
    }
}