package kspt.bank.controllers

import kspt.bank.*
import kspt.bank.dto.CellApplicationDTO
import kspt.bank.dto.CellDTO
import kspt.bank.enums.CellApplicationStatus
import kspt.bank.services.BankVaultFacade
import kspt.bank.views.client.ClientMainView
import kspt.bank.views.manager.ManagerMainView
import tornadofx.runLater

open class GeneralController : ErrorHandlingController() {
    protected val bankVaultFacade by lazy {
        BankVaultCoreApplication.getApplicationContext().getBean(BankVaultFacade::class.java)
    }

    protected val userModel: UserModel by inject()

    protected fun CellDTO.toCellTableEntry() =
            ClientMainView.CellTableEntry(
                    this.codeName,
                    this.status.asCellStatus(),
                    this.size.asChoosableCellSize().displayName,
                    this.containedPreciousName,
                    this.leaseBegin?.toString() ?: "",
                    this.leasePeriod.days,
                    this.applicationId)

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

}