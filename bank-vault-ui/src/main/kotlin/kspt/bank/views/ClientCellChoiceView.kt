package kspt.bank.views

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.text.Font
import kspt.bank.controllers.CellApplicationController
import tornadofx.*
import java.time.Period

class ClientCellChoiceView : View() {
    private val model = ViewModel()

    private val cellApplicationId: Int by param()

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
                    textfield(period).required()
                }
            }
        }

        anchorpane {
            button("Отмена" ) {
                anchorpaneConstraints { leftAnchor = 0 }
                action {
                    find(ClientCellChoiceView::class).replaceWith(ClientMainView::class, sizeToScene = true)
                }

            }
            button("Дальше") {
                anchorpaneConstraints { rightAnchor = 0 }
                enableWhen(model.valid)
                action {
                    println("Выбранный размер = ${selectedSize.value}")
                    cellApplicationController.processCellRequest(selectedSize.value,
                            Period.ofDays(period.value.toInt()), cellApplicationId)
                }
            }
        }
    }
}

enum class ChoosableCellSize(val displayName: String) {
    SMALL ("Малый"), MEDIUM ("Средний"), BIG ("Большой");

    override fun toString(): String {
        return this.displayName
    }
}