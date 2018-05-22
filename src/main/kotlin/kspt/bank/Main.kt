package kspt.bank

import javafx.application.Application
import kspt.bank.views.ClientMainView
import tornadofx.*

class MyApp: App(ClientMainView::class)

fun main(args: Array<String>) {
    Application.launch(MyApp::class.java, *args)
}