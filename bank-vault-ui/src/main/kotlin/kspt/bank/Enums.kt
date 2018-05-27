package kspt.bank

enum class ChoosableCellSize(val displayName: String) {
    SMALL ("Малый"), MEDIUM ("Средний"), BIG ("Большой");

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