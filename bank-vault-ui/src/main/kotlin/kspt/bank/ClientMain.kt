package kspt.bank

import javafx.application.Application
import kspt.bank.views.client.ClientMainView
import kspt.bank.views.client.LoginView
import tornadofx.find

class ClientApp: BankVaultApp() {
    override val primaryView = LoginView::class

    override fun stop() {
        find(ClientMainView::class).timerExecutor.shutdownNow()
        find(ClientMainView::class).tableUpdater.shutdownNow()
        super.stop()
    }
}

fun main(args: Array<String>) {
    BankVaultApp.argv = args
    Application.launch(ClientApp::class.java, *args)
}