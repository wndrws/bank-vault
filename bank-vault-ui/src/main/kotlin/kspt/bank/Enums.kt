package kspt.bank

import kspt.bank.enums.CellApplicationStatus
import kspt.bank.enums.CellSize

enum class ChoosableCellSize(val displayName: String) {
    SMALL ("Малый (1 л)"), MEDIUM ("Средний (2 л)"), BIG ("Большой (4 л)");

    override fun toString(): String {
        return this.displayName
    }
}

enum class CellStatus(val displayName: String) {
    BOOKED ("Ожидает одобрения"), AWAITING ("Ождиает оплаты"), PAID ("Оплачена");

    override fun toString(): String {
        return this.displayName
    }
}

enum class ChoosablePaymentMethod(val displayName: String) {
    CARD ("Банковская карта"), CASH ("Наличные");

    override fun toString(): String {
        return this.displayName
    }
}

fun CellSize.asChoosableCellSize(): ChoosableCellSize {
    return when (this) {
        CellSize.SMALL -> ChoosableCellSize.SMALL
        CellSize.MEDIUM -> ChoosableCellSize.MEDIUM
        CellSize.BIG -> ChoosableCellSize.BIG
    }
}

fun CellApplicationStatus.asCellStatus(): CellStatus {
    return when (this) {
        CellApplicationStatus.CELL_CHOSEN -> CellStatus.BOOKED;
        CellApplicationStatus.APPROVED -> CellStatus.AWAITING;
        CellApplicationStatus.PAID -> CellStatus.PAID;
        else -> throw IllegalStateException("Non-convertible cell application status!")
    }
}