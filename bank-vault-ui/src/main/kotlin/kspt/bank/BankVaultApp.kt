package kspt.bank

import kspt.bank.controllers.NotificationController
import tornadofx.App

open class BankVaultApp : App() {
    private val notificationController: NotificationController by inject()

    init {
        BankVaultCoreApplication.start(notificationController);
    }

    override fun stop() {
        BankVaultCoreApplication.shutdown();
        super.stop()
    }
}