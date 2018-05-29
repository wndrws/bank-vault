package kspt.bank.views

import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.control.ListView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane
import kspt.bank.controllers.CellApplicationController
import kspt.bank.dto.CellApplicationDTO
import kspt.bank.dto.ClientDTO
import tornadofx.*
import java.time.Period

class ManagerMainView : View("Bank Vault") {
    override val root: AnchorPane by fxml("/fxml/ManagerMain.fxml")

    private val cellApplicationController: CellApplicationController by inject()

    private val cellApplicationList: ListView<CellApplicationListEntry> by fxid("listCellApplications")

    private val infoPane: Pane by fxid("infoPane")

    val cellApplicationListItems = FXCollections.observableArrayList<CellApplicationListEntry>()!!

    private val model = CellApplicationListEntryModel()

    init {
        configureClientInfoPane();
        configureListCellsFormatting()
        cellApplicationList.bindSelected(model)
        cellApplicationList.items = cellApplicationListItems
    }

    private fun configureClientInfoPane() {
        infoPane.add(
                form {
                    fieldset("Клиент") {
                        field("Паспорт: ") {
                            label(model.passport)
                        }
                        field("ФИО: ") {
                            label(model.fullname)
                        }
                        field("Дата рождения: ") {
                            label(model.birthday)
                        }
                        field("Телефон: ") {
                            label(model.phone)
                        }
                        field("E-mail: ") {
                            label(model.email)
                        }
                    }
                    fieldset("Параметры аренды") {
                        field("Ячейка: ") {
                            label(model.cell)
                        }
                        field("Период: ") {
                            label(model.period)
                        }
                        field("Стоимость: ") {
                            label(model.cost)
                        }
                    }
                }
        )
    }

    private fun configureListCellsFormatting() {
        cellApplicationList.cellFormat {
            val cellCode = this.item.cellCode
            val clientName = this.item.client
            graphic = vbox {
                spacing = 3.0
                padding = Insets(8.0)
                label("Ячейка $cellCode")
                label(clientName)
            }
        }
    }

    override fun onDock() {
        super.onDock()
        refresh()
    }

    fun refresh() {
        cellApplicationController.fillCellApplicationList()
    }

    fun approve() {
        if (!model.isEmpty) {
            cellApplicationController.approveApplication(model.item.info.id)
            cellApplicationListItems.remove(model.item)
            model.item = null
        }
    }

    fun decline() {
        if (!model.isEmpty) {
            cellApplicationController.declineApplication(model.item.info.id)
            cellApplicationListItems.remove(model.item)
            model.item = null
        }
    }

    data class CellApplicationListEntry(
            val cellCode: String, val client: String, val info: CellApplicationDTO)

    class CellApplicationListEntryModel : ItemViewModel<CellApplicationListEntry>() {
        val passport = bind { item?.info?.getLeaseholder()?.passportSerial?.toProperty() }
        val fullname = bind { item?.info?.getLeaseholder()?.let { getFullName(it) }.toProperty() }
        val birthday = bind { item?.info?.getLeaseholder()?.birthday?.toString()?.toProperty() }
        val phone = bind { item?.info?.getLeaseholder()?.phone?.toString()?.toProperty() }
        val email = bind { item?.info?.getLeaseholder()?.email?.toString()?.toProperty() }
        val cell = bind { item?.cellCode?.toProperty() }
        val period = bind { item?.info?.getLeasePeriod()?.let { formatPeriod(it) }?.toProperty() }
        val cost = bind { item?.info?.getLeaseCost()?.let { formatMoney(it) }?.toProperty() }

        private fun getFullName(client: ClientDTO) =
                "${client.lastName} ${client.firstName} ${client.patronymic}"

        private fun formatPeriod(period: Period) = "${period.days} дн."

        private fun formatMoney(amount: Long) = "$amount руб."
    }
}