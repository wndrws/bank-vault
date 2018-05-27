package kspt.bank.external;

import kspt.bank.enums.PaymentMethod;

public class SimplePaymentSystem implements PaymentGate {
    @Override
    public Invoice issueInvoice(long sum) {
        return new Invoice(sum);
    }

    @Override
    public long pay(Invoice invoice, long sum, PaymentMethod paymentMethod) {
        if (sum < invoice.getSum()) {
            throw new PaymentException("Not enough money: " + sum + " when needed " + invoice.getSum());
        } else {
            invoice.markAsPaid();
            return sum - invoice.getSum();
        }
    }

    public static class PaymentException extends RuntimeException {
        PaymentException(final String msg) {
            super(msg);
        }
    }
}
