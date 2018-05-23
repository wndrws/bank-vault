package kspt.bank

import javafx.application.Application
import kspt.bank.views.ClientMainView
import tornadofx.App
import tornadofx.runAsync

class MyApp: App(ClientMainView::class) {
    private val server: WebServer by inject()

    init {
        runAsync { server.start() }
    }

    override fun stop() {
        server.stop()
        super.stop()
    }
}

fun main(args: Array<String>) {
    Application.launch(MyApp::class.java, *args)
}