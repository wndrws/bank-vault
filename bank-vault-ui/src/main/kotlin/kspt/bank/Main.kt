package kspt.bank

import javafx.application.Application
import kspt.bank.controllers.NotificationController
import kspt.bank.views.ClientMainView
import kspt.bank.views.LoginView
import tornadofx.App

class MyApp: App(LoginView::class) {
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
    Application.launch(MyApp::class.java, *args)
}