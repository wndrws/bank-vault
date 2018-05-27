package kspt.bank.controllers

import javafx.stage.StageStyle
import kspt.bank.logger
import kspt.bank.views.ErrorModalView
import tornadofx.Controller
import java.lang.Exception

abstract class ErrorHandlingController : Controller() {
    protected val logger = logger()

    protected fun displayError(e: Exception) {
        val errorWindow = find<ErrorModalView>("message" to (e.message ?: "unknown"))
        errorWindow.openModal(stageStyle = StageStyle.UTILITY)
    }
}