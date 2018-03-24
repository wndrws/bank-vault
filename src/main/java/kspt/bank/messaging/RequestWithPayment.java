package kspt.bank.messaging;

import kspt.bank.domain.entities.PaymentMethod;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RequestWithPayment extends Request {
    public final Long paymentSum;

    public final PaymentMethod paymentMethod;
}
