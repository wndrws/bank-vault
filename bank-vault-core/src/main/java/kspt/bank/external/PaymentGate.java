package kspt.bank.external;

import kspt.bank.enums.PaymentMethod;

public interface PaymentGate {
    Invoice issueInvoice(long sum);

    long pay(Invoice invoice, long sum, PaymentMethod paymentMethod);
}
