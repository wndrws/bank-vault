package kspt.bank.external;

import kspt.bank.enums.PaymentMethod;

public interface PaymentGate {
    Invoice issueInvoice(long sum, int applicationId);

    Integer findGood(final Invoice invoice);

    long pay(Invoice invoice, long sum, PaymentMethod paymentMethod);
}
