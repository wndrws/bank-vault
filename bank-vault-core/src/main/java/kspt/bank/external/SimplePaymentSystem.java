package kspt.bank.external;

import kspt.bank.enums.PaymentMethod;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class SimplePaymentSystem implements PaymentGate {
    private final Map<Invoice, Integer> invoiceToGoodId;

    public SimplePaymentSystem() {
        invoiceToGoodId = new HashMap<>();
    }

    @Override
    public Invoice issueInvoice(long sum, int goodId) {
        final Invoice invoice = new Invoice(sum);
        invoiceToGoodId.put(invoice, goodId);
        return invoice;
    }

    @Override
    public Integer findGood(Invoice invoice) {
        return invoiceToGoodId.get(invoice);
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
