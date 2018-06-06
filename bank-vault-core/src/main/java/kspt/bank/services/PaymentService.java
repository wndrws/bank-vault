package kspt.bank.services;

import kspt.bank.enums.PaymentMethod;
import kspt.bank.external.Invoice;
import kspt.bank.external.PaymentSystem;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class PaymentService {
    @Autowired
    private final PaymentSystem paymentSystem;

    public Optional<Invoice> getInvoiceForApplication(final Integer appId) {
        return Optional.ofNullable(paymentSystem.findInvoice(appId));
    }

    public long pay(final Invoice invoice, final long sum, final PaymentMethod paymentMethod) {
        return paymentSystem.pay(invoice, sum, paymentMethod);
    }
}
