package kspt.bank.controllers

import kspt.bank.boundaries.NotificationGate
import kspt.bank.domain.entities.Cell
import kspt.bank.domain.entities.Client
import tornadofx.Controller
import java.time.LocalDate

class NotificationController : Controller(), NotificationGate {
    override fun notifyManager(message: String?) {
        println(message)
    }

    override fun notifyManagerAboutLeasingEnd(cell: Cell?) {
        TODO("not implemented")
    }

    override fun notifyClient(client: Client?, message: String?) {
        TODO("not implemented")
    }

    override fun notifyClientAboutArrangement(client: Client?, message: String?, date: LocalDate?) {
        TODO("not implemented")
    }

    override fun notifyClientAboutLeasingExpiration(client: Client?, cell: Cell?) {
        TODO("not implemented")
    }
}