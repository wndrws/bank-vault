package kspt.bank.external;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.json.UTF8JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kspt.bank.enums.PaymentMethod;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Slf4j
public class FileBasedPaymentSystem extends SimplePaymentSystem {
    private final File file = new File("invoices.json");

    private final ObjectMapper mapper = new ObjectMapper();

    public FileBasedPaymentSystem() {
        super();
        updateInvoiceInfo();
    }

    @Override
    public Invoice issueInvoice(long sum, int goodId) {
        final Invoice invoice = super.issueInvoice(sum, goodId);
        flush();
        return invoice;
    }

    private void flush() {
        try {
            final ArrayNode root = mapper.createArrayNode();
            invoiceToGoodId.forEach((invoice, goodId) -> {
                final ObjectNode entryNode = mapper.createObjectNode();
                entryNode.set("invoice", mapper.valueToTree(invoice));
                entryNode.put("goodId", goodId);
                root.add(entryNode);
            });
            mapper.writeValue(file, root);
        } catch (IOException e) {
            log.error("Cannot write invoices!", e);
        }
    }

    @Override
    public Integer findGood(Invoice invoice) {
        updateInvoiceInfo();
        return super.findGood(invoice);
    }

    private void updateInvoiceInfo() {
        if (invoiceToGoodId.isEmpty() && !file.canRead()) return;
        try {
            final ArrayNode root = (ArrayNode) mapper.readTree(file);
            root.forEach(node -> {
                final Invoice invoice = mapper.convertValue(node.get("invoice"), Invoice.class);
                final Integer goodId = node.get("goodId").asInt();
                invoiceToGoodId.put(invoice, goodId);
            });
        } catch (IOException e) {
            log.error("Cannot read invoices!", e);
        }
    }

    @Override
    public long pay(Invoice invoice, long sum, PaymentMethod paymentMethod) {
        final long change = super.pay(invoice, sum, paymentMethod);
        flush();
        return change;
    }
}
