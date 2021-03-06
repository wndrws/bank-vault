package kspt.bank.views.client

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.geometry.Insets
import kspt.bank.ChoosableCellSize
import kspt.bank.controllers.CellApplicationController
import tornadofx.*

class CellChoiceView : View() {
    private val model = ViewModel()

    private val cellSizes = FXCollections.observableArrayList(*ChoosableCellSize.values())

    private val selectedSize = model.bind { SimpleObjectProperty<ChoosableCellSize>() }

    private val period = model.bind { SimpleIntegerProperty() }

    private val cellApplicationController: CellApplicationController by inject()

    override val root = vbox {
        padding = Insets(20.0)

        form {
            fieldset("Выбор ячейки") {
                field("Желаемый размер") {
                    combobox(selectedSize, cellSizes).required()
                }
                field("Период аренды (дней):") {
                    textfield(period).validator {
                        if (it.isNullOrBlank()) error("This field is required")
                        else if (it?.isInt() != true) error("Введите число")
                        else if (it.toInt() <= 0) error("Недопустимое число")
                        else null
                    }
                }
            }
        }

        anchorpane {
            button("Отмена" ) {
                anchorpaneConstraints { leftAnchor = 0 }
                action {
                    find(CellChoiceView::class).replaceWith(ClientMainView::class, sizeToScene = true)
                }

            }
            button("Дальше") {
                anchorpaneConstraints { rightAnchor = 0 }
                enableWhen(model.valid)
                action {
                    cellApplicationController.processCellRequest(selectedSize.value, period.value.toInt())
                }
            }
        }
    }
}