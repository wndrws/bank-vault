package kspt.bank.boundaries;

import kspt.bank.domain.entities.PaymentMethod;

public interface PaymentGate {
    void acceptPayment(long sum, PaymentMethod paymentMethod);
}
