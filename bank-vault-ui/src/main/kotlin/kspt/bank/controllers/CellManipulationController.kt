package kspt.bank.controllers

import kspt.bank.dto.PreciousDTO

class CellManipulationController : GeneralController() {
    fun putPrecious(applicationId: Int, volume: Int, name: String) {
        errorAware {
            bankVaultFacade.putPrecious(applicationId, PreciousDTO(volume, name))
            fillCellsTable()
        }
    }

    fun getPrecious(applicationId: Int) : PreciousDTO? {
        var precious: PreciousDTO? = null
        errorAware {
            precious = bankVaultFacade.getPrecious(applicationId)
            fillCellsTable()
        }
        return precious
    }
}