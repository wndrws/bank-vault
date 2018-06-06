package kspt.bank.external;

import kspt.bank.enums.PaymentMethod;

public interface PaymentSystem {
    Invoice issueInvoice(long sum, int applicationId);

    Integer findGood(final Invoice invoice);

    Invoice findInvoice(final int goodId);

    long pay(Invoice invoice, long sum, PaymentMethod paymentMethod);
}
