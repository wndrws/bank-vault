package kspt.bank

import javafx.application.Application
import kspt.bank.controllers.NotificationController
import kspt.bank.views.ClientMainView
import kspt.bank.views.LoginView
import kspt.bank.views.ManagerMainView
import tornadofx.App
import tornadofx.find

class ClientApp: BankVaultApp() {
    override val primaryView = LoginView::class

    override fun stop() {
        find(ClientMainView::class).timerExecutor.shutdownNow()
        super.stop()
    }
}

fun main(args: Array<String>) {
    BankVaultApp.argv = args
    Application.launch(ClientApp::class.java, *args)
}