package kspt.bank

import javafx.application.Application
import kspt.bank.controllers.NotificationController
import kspt.bank.views.LoginView
import kspt.bank.views.ManagerMainView
import tornadofx.App

class ClientApp: BankVaultApp() {
    override val primaryView = LoginView::class
}

fun main(args: Array<String>) {
    Application.launch(ClientApp::class.java, *args)
}