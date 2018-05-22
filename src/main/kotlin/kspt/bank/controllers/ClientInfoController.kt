package kspt.bank.controllers

import javafx.stage.StageStyle
import kspt.bank.ClientInfoServiceBean
import kspt.bank.views.ErrorModalView
import tornadofx.*
import java.lang.Exception
import java.time.LocalDate

class ClientInfoController : Controller() {
    private val clientInfoServiceBean: ClientInfoServiceBean by inject()

    fun processClientInfo(serial: String, firstName: String, lastName: String, patronymic: String,
                          birthday: LocalDate, email: String, phone: String) {
        try {
            clientInfoServiceBean.service.acceptClientInfo(
                    serial, firstName, lastName, patronymic, birthday, email, phone)
        } catch (e: Exception) {
            val errorWindow = find<ErrorModalView>("message" to (e.message ?: "unknown"))
            errorWindow.openModal(stageStyle = StageStyle.UTILITY)
        }
    }
}