package kspt.bank.rest;

import kspt.bank.enums.PaymentMethod;
import kspt.bank.external.Invoice;
import kspt.bank.external.SimplePaymentSystem;
import kspt.bank.services.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("external/pay")
public class PaymentRestController {
    @Autowired
    private final PaymentService paymentService;

    @GetMapping(value = "invoice/{appId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Invoice> getInvoice(@PathVariable("appId") Integer appId) {
        return paymentService.getInvoiceForApplication(appId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.unprocessableEntity().build());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Long> pay(@RequestBody Invoice invoice, @RequestParam("sum") Long sum,
            @RequestParam("method") PaymentMethod paymentMethod) {
        try {
            return ResponseEntity.ok(paymentService.pay(invoice, sum, paymentMethod));
        } catch (SimplePaymentSystem.PaymentException ex) {
            return ResponseEntity.unprocessableEntity().body(sum);
        }
    }
}
