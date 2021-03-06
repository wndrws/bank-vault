package kspt.bank.views

import javafx.geometry.Insets
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import tornadofx.Fragment

class ErrorModalView : Fragment() {
    val message: String by param()

    val errorText: Text by fxid("textMessage")

    override val root: VBox by fxml("/fxml/Error.fxml")

    init {
        errorText.text = message
        root.padding = Insets(15.0)
    }
}