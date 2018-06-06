package kspt.bank.controllers

import javafx.stage.StageStyle
import kspt.bank.BankVaultCoreApplication
import kspt.bank.CellStatus
import kspt.bank.ChoosableCellSize
import kspt.bank.dto.CellApplicationDTO
import kspt.bank.dto.CellDTO
import kspt.bank.enums.CellApplicationStatus
import kspt.bank.enums.CellSize
import kspt.bank.services.BankVaultFacade
import kspt.bank.views.ClientCellChoiceView
import kspt.bank.views.ClientMainView
import kspt.bank.views.ErrorModalView
import kspt.bank.views.ManagerMainView
import tornadofx.runLater
import java.lang.Exception
import java.time.Period

class CellApplicationController : ErrorHandlingController() {
    private val bankVaultFacade by lazy {
        BankVaultCoreApplication.getApplicationContext().getBean(BankVaultFacade::class.java)
    }

    private val userModel: UserModel by inject()

    fun processCellRequest(size: ChoosableCellSize, period: Period) {
        errorAware("processCellRequest") {
            val applicationId = bankVaultFacade.acceptClientInfo(userModel.clientInfo.value)
            val success = bankVaultFacade.requestCell(size.asCellSize(), period, applicationId)
            if (!success) {
                val errorWindow = find<ErrorModalView>(
                        "message" to "Нет доступной ячейки запрошенного размера!")
                errorWindow.openModal(stageStyle = StageStyle.UTILITY)
            } else {
                updateCellsTable(applicationId)
                find(ClientCellChoiceView::class).replaceWith(ClientMainView::class, sizeToScene = true)
            }
        }
    }

    private fun ChoosableCellSize.asCellSize(): CellSize {
        return when (this) {
            ChoosableCellSize.SMALL -> CellSize.SMALL
            ChoosableCellSize.MEDIUM -> CellSize.MEDIUM
            ChoosableCellSize.BIG -> CellSize.BIG
        }
    }

    private fun CellSize.asChoosableCellSize(): ChoosableCellSize {
        return when (this) {
            CellSize.SMALL -> ChoosableCellSize.SMALL
            CellSize.MEDIUM -> ChoosableCellSize.MEDIUM
            CellSize.BIG -> ChoosableCellSize.BIG
        }
    }

    private fun CellApplicationStatus.asCellStatus(): CellStatus {
        return when (this) {
            CellApplicationStatus.CELL_CHOSEN -> CellStatus.BOOKED;
            CellApplicationStatus.APPROVED -> CellStatus.AWAITING;
            CellApplicationStatus.PAID -> CellStatus.PAID;
            else -> throw IllegalStateException("Non-convertible cell application status!")
        }
    }

    private fun updateCellsTable(applicationId: Int) {
        val clientMainView = find(ClientMainView::class)
        val cellInfo = bankVaultFacade.findCellInfo(applicationId)
        cellInfo.ifPresent {
            clientMainView.cellTableItems.add(it.toCellTableEntry())
        }
    }

    private fun CellDTO.toCellTableEntry() =
            ClientMainView.CellTableEntry(
                    this.codeName,
                    this.status.asCellStatus().displayName,
                    this.size.asChoosableCellSize().displayName,
                    this.containedPreciousName,
                    this.leaseBegin?.toString() ?: "",
                    this.leasePeriod.days)

    fun fillCellsTable() {
        var clientsCellsInfo: List<CellDTO> = emptyList()
        errorAware("findCellsInfoByClient") {
            clientsCellsInfo = bankVaultFacade.findCellsInfoByClient(userModel.id.value.toInt())
        }
        runLater {
            find(ClientMainView::class).cellTableItems.setAll(
                    clientsCellsInfo.map { it.toCellTableEntry() })
        }
    }

    fun fillCellApplicationList() {
        var cellApplications: List<CellApplicationDTO> = emptyList()
        errorAware("findAllCellApplications") {
            cellApplications = bankVaultFacade.findAllCellApplications()
                    .filter { it.status == CellApplicationStatus.CELL_CHOSEN }
        }
        runLater {
            find(ManagerMainView::class).cellApplicationListItems.setAll(
                    cellApplications.map { it.toCellApplicationListEntry() }
            )
        }

    }

    private fun CellApplicationDTO.toCellApplicationListEntry() =
            ManagerMainView.CellApplicationListEntry(this.cell.codeName,
                    "${this.leaseholder.firstName} ${this.leaseholder.lastName}", this)

    fun approveApplication(applicationId: Int) {
        errorAware {
            bankVaultFacade.approveApplication(applicationId)
        }
    }

    fun declineApplication(applicationId: Int) {
        errorAware {
            bankVaultFacade.declineApplication(applicationId)
        }
    }
}