package kspt.bank

import kspt.bank.controllers.NotificationController
import tornadofx.App

open class BankVaultApp : App() {
    companion object {
        var argv: Array<String> = emptyArray()
    }

    private val notificationController: NotificationController by inject()

    init {
        BankVaultCoreApplication.start(notificationController, argv);
    }

    override fun stop() {
        BankVaultCoreApplication.shutdown();
        super.stop()
    }
}