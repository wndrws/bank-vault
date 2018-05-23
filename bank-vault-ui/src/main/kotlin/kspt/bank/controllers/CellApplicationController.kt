package kspt.bank.controllers

import javafx.stage.StageStyle
import kspt.bank.ApplicationsRepositoryBean
import kspt.bank.CellApplicationServiceBean
import kspt.bank.domain.entities.CellSize
import kspt.bank.views.*
import tornadofx.*
import java.lang.Exception
import java.time.LocalDate
import java.time.Period

class CellApplicationController : Controller() {
    private val cellApplicationServiceBean: CellApplicationServiceBean by inject()

    fun processClientInfo(serial: String, firstName: String, lastName: String, patronymic: String,
                          birthday: LocalDate, email: String, phone: String) {
        try {
            val applicationId = cellApplicationServiceBean.service.acceptClientInfo(
                    serial, firstName, lastName, patronymic, birthday, email, phone)
            val next = find<ClientCellChoiceView>("cellApplicationId" to applicationId)
            find(ClientInfoView::class).replaceWith(next, sizeToScene = true)
        } catch (e: Exception) {
            displayError(e)
            e.printStackTrace()
        }
    }

    private fun displayError(e: Exception) {
        val errorWindow = find<ErrorModalView>("message" to (e.message ?: "unknown"))
        errorWindow.openModal(stageStyle = StageStyle.UTILITY)
    }

    fun processCellRequest(size: ChoosableCellSize, period: Period, applicationId: Int) {
        try {
            val success = cellApplicationServiceBean.service.requestCell(
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
            e.printStackTrace()
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
        val applicationsRepositoryBean = find(ApplicationsRepositoryBean::class)
        val app = applicationsRepositoryBean.repository.find(applicationId)
        clientMainView.cellTableItems.add(CellTableEntry(app.cell?.id ?: -1, app.status.name,
                app.cell?.size?.asChoosableCellSize()?.toString() ?: "",
                app.cell?.containedPrecious?.name ?: "", app.leasePeriod?.
                let { LocalDate.now().plusDays(it.days.toLong()) } ?.toString() ?: ""))
    }
}