package kspt.bank.views.client

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.stage.StageStyle
import kspt.bank.ChoosablePaymentMethod
import kspt.bank.controllers.CellApplicationController
import kspt.bank.external.Invoice
import kspt.bank.views.ErrorModalView
import kspt.bank.views.PaymentResultView
import tornadofx.*

class PaymentView : View() {
    private val model = ViewModel()

    private val paymentMethods = FXCollections.observableArrayList(*ChoosablePaymentMethod.values())

    private val selectedMethod = model.bind { SimpleObjectProperty<ChoosablePaymentMethod>() }

    private var toPay = model.bind { SimpleIntegerProperty() }

    private var invoiceId = model.bind { SimpleIntegerProperty() }

    private val sum = model.bind { SimpleIntegerProperty() }

    private val cellApplicationController: CellApplicationController by inject()

    private val applicationId: Int by param()

    private var invoice: Invoice? = null

    override val root = vbox {
        padding = Insets(20.0)

        form {
            fieldset("Оплата аренды") {
                field("Счет №:") {
                    label(invoiceId)
                }
                field("К оплате (руб.):") {
                    label(toPay)
                }
                field("Способ оплаты:") {
                    combobox(selectedMethod, paymentMethods).required()
                }
                field("Сумма:") {
                    textfield(sum).validator {
                        if (it.isNullOrBlank()) error("This field is required")
                        else if (it?.isInt() != true) error("Введите число")
                        else if (it.toInt() <= 0) error("Недопустимое число")
                        else null
                    }
                }
            }
        }

        anchorpane {
            button("Отмена" ) {
                anchorpaneConstraints { leftAnchor = 0 }
                action {
                    this@PaymentView.close()
                }

            }
            button("Оплатить") {
                anchorpaneConstraints { rightAnchor = 0 }
                enableWhen(model.valid)
                action {
                    payForInvoice()
                    this@PaymentView.close()
                }
            }
        }
    }

    private fun payForInvoice() {
        invoice?.let {
            val change = cellApplicationController.payForCell(
                    it, sum.value.toLong(), selectedMethod.value)
            val success = cellApplicationController.acceptPayment(it)
            if (success) {
                find<PaymentResultView>("message" to "Оплата прошла успешно!\nСдача: $change руб.")
                        .openModal(stageStyle = StageStyle.UTILITY)
            } else {
                find<ErrorModalView>("message" to "Оплата не прошла!")
                        .openModal(stageStyle = StageStyle.UTILITY)
            }
        }
    }

    override fun onDock() {
        super.onDock()
        invoice = cellApplicationController.getPaymentInfo(applicationId)
        invoiceId.value = invoice?.id
        toPay.value = invoice?.sum
        if (invoice == null) {
            this@PaymentView.close()
        }
    }
}