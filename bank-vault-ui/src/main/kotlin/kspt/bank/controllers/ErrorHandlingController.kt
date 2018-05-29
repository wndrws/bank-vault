package kspt.bank.controllers

import javafx.stage.StageStyle
import kspt.bank.logger
import kspt.bank.views.ErrorModalView
import tornadofx.Controller
import java.lang.Exception

abstract class ErrorHandlingController : Controller() {
    protected val logger = logger()

    protected fun errorAware(action: () -> Unit) = try {
        action()
    } catch (e: Exception) {
        displayError(e)
        logger.error("Error", e)
    }

    protected fun errorAware(context: String, action: () -> Unit): Unit = try {
        action()
    } catch (e: Exception) {
        displayError(e)
        logger.error("Error in $context", e)
    }

    protected fun displayError(e: Exception) {
        val errorWindow = find<ErrorModalView>("message" to (e.message ?: "unknown"))
        errorWindow.openModal(stageStyle = StageStyle.UTILITY)
    }
}