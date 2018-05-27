package kspt.bank.controllers

import javafx.stage.StageStyle
import kspt.bank.domain.entities.CellSize
import kspt.bank.services.BankVaultCoreApplication
import kspt.bank.services.BankVaultFacade
import kspt.bank.views.*
import java.lang.Exception
import java.time.LocalDate
import java.time.Period

class CellApplicationController : ErrorHandlingController() {
    private val bankVaultFacade by lazy {
        BankVaultCoreApplication.getApplicationContext().getBean(BankVaultFacade::class.java)
    }

    fun processClientInfo(serial: String, firstName: String, lastName: String, patronymic: String,
                          birthday: LocalDate, email: String, phone: String) {
        try {
            val applicationId = bankVaultFacade.acceptClientInfo(
                    serial, firstName, lastName, patronymic, birthday, email, phone)
            val next = find<ClientCellChoiceView>("cellApplicationId" to applicationId)
            find(ClientInfoView::class).replaceWith(next, sizeToScene = true)
        } catch (e: Exception) {
            displayError(e)
            logger.error("Failed to process client info", e)
        }
    }

    fun processCellRequest(size: ChoosableCellSize, period: Period, applicationId: Int) {
        try {
            val success = bankVaultFacade.requestCell(
                    size.asCellSize(), period, applicationId)
            if (!success) {
                val errorWindow = find<ErrorModalView>(
                        "message" to "Нет доступной ячейки запрошенного размера!")
                errorWindow.openModal(stageStyle = StageStyle.UTILITY)
            } else {
                println("Успех!")
                updateCellsTable(applicationId)
                find(ClientCellChoiceView::class).replaceWith(ClientMainView::class, sizeToScene = true)
            }
        } catch (e: Exception) {
            displayError(e)
            logger.error("Failed to process cell request", e)
        }
    }

    private fun ChoosableCellSize.asCellSize() : CellSize {
        return when(this) {
            ChoosableCellSize.SMALL -> CellSize.SMALL
            ChoosableCellSize.MEDIUM -> CellSize.MEDIUM
            ChoosableCellSize.BIG -> CellSize.BIG
        }
    }

    private fun CellSize.asChoosableCellSize() : ChoosableCellSize {
        return when(this) {
            CellSize.SMALL -> ChoosableCellSize.SMALL
            CellSize.MEDIUM -> ChoosableCellSize.MEDIUM
            CellSize.BIG -> ChoosableCellSize.BIG
        }
    }

    private fun updateCellsTable(applicationId: Int) {
        val clientMainView = find(ClientMainView::class)
        val app = bankVaultFacade.findCellApplication(applicationId)
        clientMainView.cellTableItems.add(CellTableEntry(app.cell?.id ?: -1, app.status.name,
                app.cell?.size?.asChoosableCellSize()?.toString() ?: "",
                app.cell?.containedPrecious?.name ?: "", app.leasePeriod?.
                let { LocalDate.now().plusDays(it.days.toLong()) } ?.toString() ?: ""))
    }
}