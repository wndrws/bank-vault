package kspt.bank.views.client

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import kspt.bank.controllers.CellManipulationController
import tornadofx.*

class PutPreciousView : View() {
    private val model = ViewModel()

    private val name = model.bind { SimpleStringProperty() }

    private val volume = model.bind { SimpleIntegerProperty() }

    private val cellManipulationController: CellManipulationController by inject()

    private val applicationId: Int by param()

    override val root = vbox {
        padding = Insets(20.0)

        form {
            fieldset("Сведения о ценности") {
                field("Объем (л):") {
                    textfield(volume).validator {
                        if (it.isNullOrBlank()) error("This field is required")
                        else if (it?.isDouble() != true) error("Введите число")
                        else if (it.toDouble() <= 0) error("Недопустимое число")
                        else null
                    }
                }
                field("Краткое описание:") {
                    textfield(name).required()
                }
            }
        }

        anchorpane {
            button("Отмена" ) {
                anchorpaneConstraints { leftAnchor = 0 }
                action {
                    this@PutPreciousView.close()
                }

            }
            button("ОК") {
                anchorpaneConstraints { rightAnchor = 0 }
                enableWhen(model.valid)
                action {
                    cellManipulationController.putPrecious(
                            applicationId, volume.value.toInt(), name.value)
                    this@PutPreciousView.close()
                }
            }
        }
    }
}