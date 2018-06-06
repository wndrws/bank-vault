package kspt.bank.external;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import kspt.bank.enums.PaymentMethod;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimplePaymentSystem implements PaymentSystem {
    protected final BiMap<Invoice, Integer> invoiceToGoodId;

    public SimplePaymentSystem() {
        invoiceToGoodId = HashBiMap.create();
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
    public Invoice findInvoice(int goodId) {
        return invoiceToGoodId.inverse().get(goodId);
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
