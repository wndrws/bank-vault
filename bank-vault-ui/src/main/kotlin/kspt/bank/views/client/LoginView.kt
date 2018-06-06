package kspt.bank.views.client

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import kspt.bank.controllers.LoginController
import tornadofx.*

class LoginView : View("Bank Vault - Login") {
    private val model = ViewModel()

    private val username = model.bind { SimpleStringProperty() }

    private val password = model.bind { SimpleStringProperty() }

    private val loginController: LoginController by inject()

    override val root = form {
        padding = Insets(20.0)

        label("Банковское\nхранилище\n") {
            font = Font.font("System", FontWeight.BOLD, 24.0)
        }

        fieldset(labelPosition = Orientation.VERTICAL) {
            spacing = 10.0
            field("Логин:") {
                textfield(username).required()
            }
            field("Пароль:") {
                passwordfield(password).required()
            }
            button("Войти") {
                enableWhen(model.valid)
                isDefaultButton = true
                useMaxWidth = true
                action {
                    loginController.login(username.value, password.value)
                }
            }
            button("Зарегистрироваться") {
                enableWhen(model.valid)
                useMaxWidth = true
                action {
                    loginController.proceedToRegister(username.value, password.value)
                }
            }
        }
        label(loginController.statusProperty) {
            style {
                paddingTop = 10
                textFill = Color.RED
                fontWeight = FontWeight.BOLD
            }
        }
    }

    override fun onDock() {
        username.value = ""
        password.value = ""
        model.clearDecorators()
    }
}