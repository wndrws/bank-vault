package kspt.bank

import javafx.application.Application
import kspt.bank.views.manager.ManagerMainView

class ManagerApp: BankVaultApp() {
    override val primaryView = ManagerMainView::class
}

fun main(args: Array<String>) {
    Application.launch(ManagerApp::class.java, *args)
}