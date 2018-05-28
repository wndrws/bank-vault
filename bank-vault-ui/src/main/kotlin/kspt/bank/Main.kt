package kspt.bank

import javafx.application.Application
import kspt.bank.controllers.NotificationController
import kspt.bank.views.LoginView
import kspt.bank.views.ManagerMainView
import tornadofx.App

class ClientApp: App(LoginView::class) {
    private val notificationController: NotificationController by inject()

    init {
        BankVaultCoreApplication.start(notificationController);
    }

    override fun stop() {
        BankVaultCoreApplication.shutdown();
        super.stop()
    }
}

class ManagerApp: App(ManagerMainView::class) {
    private val notificationController: NotificationController by inject()

    init {
        BankVaultCoreApplication.start(notificationController);
    }

    override fun stop() {
        BankVaultCoreApplication.shutdown();
        super.stop()
    }
}

fun main(args: Array<String>) {
    when {
        args.isEmpty() -> println("Application type not specified!")
        args[0] == "client" -> Application.launch(ClientApp::class.java, *args)
        args[0] == "manager" -> Application.launch(ManagerApp::class.java, *args)
    }

}