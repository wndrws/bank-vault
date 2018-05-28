package kspt.bank.controllers

import javafx.beans.property.*
import kspt.bank.BankVaultCoreApplication
import kspt.bank.dto.ClientDTO
import kspt.bank.recognition.Credentials
import kspt.bank.services.LoginService
import kspt.bank.views.ClientInfoView
import kspt.bank.views.ClientMainView
import kspt.bank.views.LoginView
import tornadofx.*

class LoginController : Controller() {
    private val loginService by lazy {
        BankVaultCoreApplication.getApplicationContext().getBean(LoginService::class.java)
    }

    val statusProperty = SimpleStringProperty("")
    var status by statusProperty

    val userModel: UserModel by inject()

    fun login(username: String, password: String) {
        val userId = loginService.login(Credentials(username, password))
        if (userId.isPresent) {
            val user = User(userId.get(), loginService.getUserInfo(userId.get()).orElseThrow {
                IllegalStateException("No info for registered user!")
            })
            runLater {
                userModel.item = user
                find(LoginView::class).replaceWith(ClientMainView::class, sizeToScene = true)
            }
        } else {
            status = "Неверный логин или пароль"
        }
    }

    fun proceedToRegister(username: String, password: String) {
        val next = find<ClientInfoView>("username" to username, "password" to password)
        find(LoginView::class).replaceWith(next, sizeToScene = true)
    }

    fun register(username: String, password: String, clientInfo: ClientDTO) {
        runLater { status = "" }
        val userId = loginService.registerUser(Credentials(username, password), clientInfo)
        userModel.item = User(userId, clientInfo)
        find(ClientInfoView::class).replaceWith(ClientMainView::class, sizeToScene = true)
    }

    fun logout() {
        userModel.item = null
        primaryStage.uiComponent<UIComponent>()?.replaceWith(LoginView::class, sizeToScene = true)
    }
}

class User(id: Int, clientInfo: ClientDTO) {
    val idProperty = SimpleIntegerProperty(this, "id", id)
    var id by idProperty

    val clientInfoProperty = SimpleObjectProperty<ClientDTO>(this, "clientInfo", clientInfo)
    var clientInfo by clientInfoProperty
}

class UserModel : ItemViewModel<User>() {
    val id = bind(User::idProperty)
    val clientInfo = bind(User::clientInfoProperty)
}