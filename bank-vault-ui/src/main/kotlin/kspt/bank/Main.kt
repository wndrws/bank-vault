package kspt.bank

import javafx.application.Application
import kspt.bank.services.BankVaultCoreApplication
import kspt.bank.views.ClientMainView
import tornadofx.App

class MyApp: App(ClientMainView::class) {
    init {
        BankVaultCoreApplication.start();
    }

    override fun stop() {
        BankVaultCoreApplication.shutdown();
        super.stop()
    }
}

fun main(args: Array<String>) {
    Application.launch(MyApp::class.java, *args)
}