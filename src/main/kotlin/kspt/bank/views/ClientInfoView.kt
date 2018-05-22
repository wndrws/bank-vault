package kspt.bank.views

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import kspt.bank.controllers.ClientInfoController
import tornadofx.*
import java.time.LocalDate

class ClientInfoView : View("Bank Vault") {
    private val model = ViewModel()

    private val serial = model.bind { SimpleStringProperty() }
    private val lastName = model.bind { SimpleStringProperty() }
    private val firstName = model.bind { SimpleStringProperty() }
    private val patronymic = model.bind { SimpleStringProperty("") }
    private val birthday = model.bind { SimpleObjectProperty<LocalDate>() }
    private val phone = model.bind { SimpleStringProperty("") }
    private val email = model.bind { SimpleStringProperty("") }

    private val clientInfoController: ClientInfoController by inject()

    override val root = vbox {
        padding = Insets(20.0)

        form {
            fieldset("Паспортные  данные") {
                field("Серия и номер") {
                    textfield(serial).required()
                }
                field("Фамилия") {
                    textfield(lastName).required()
                }
                field("Имя") {
                    textfield(firstName).required()
                }
                field("Отчество") {
                    textfield(patronymic)
                }
                field("Дата рождения") {
                    datepicker(birthday).required()
                }
            }
            fieldset("Контактные данные") {
                field("Телефон") {
                    textfield(phone)
                }
                field("E-mail") {
                    textfield(email)
                }
            }
        }

        anchorpane {
            button("Назад" ) {
                anchorpaneConstraints { leftAnchor = 0 }
                action {
                    find(ClientInfoView::class).replaceWith(ClientMainView::class, sizeToScene = true)
                }

            }
            button("Дальше") {
                anchorpaneConstraints { rightAnchor = 0 }
                enableWhen(model.valid)
                action {
                    clientInfoController.processClientInfo(
                            serial.value,
                            firstName.value,
                            lastName.value,
                            patronymic.value,
                            birthday.value,
                            email.value,
                            phone.value
                    )
                }
            }
        }
    }
}