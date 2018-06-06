package kspt.bank.controllers

import javafx.stage.StageStyle
import kspt.bank.logger
import kspt.bank.views.ErrorModalView
import tornadofx.Controller
import java.lang.Exception

abstract class ErrorHandlingController : Controller() {
    protected val logger = logger()

    protected fun errorAware(action: () -> Unit): Boolean = try {
        action()
        true
    } catch (e: Exception) {
        displayError(e)
        logger.error("Error", e)
        false
    }

    protected fun errorAware(context: String, action: () -> Unit): Boolean = try {
        action()
        true
    } catch (e: Exception) {
        displayError(e)
        logger.error("Error in $context", e)
        false
    }

    protected fun displayError(e: Exception) {
        val errorWindow = find<ErrorModalView>("message" to (e.message ?: "unknown"))
        errorWindow.openModal(stageStyle = StageStyle.UTILITY)
    }
}