package kspt.bank.external;

import kspt.bank.domain.entities.PaymentMethod;

public interface PaymentGate {
    Invoice issueInvoice(long sum);

    long pay(Invoice invoice, long sum, PaymentMethod paymentMethod);
}
