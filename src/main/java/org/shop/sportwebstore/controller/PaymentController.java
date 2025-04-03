package org.shop.sportwebstore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shop.sportwebstore.model.dto.OrderDto;
import org.shop.sportwebstore.service.store.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPayment(@RequestBody OrderDto orderDto) {
        String paymentUrl = paymentService.createPayment(orderDto);
        return new ResponseEntity<>(paymentUrl, HttpStatus.CREATED);
    }

    @PostMapping("/repay")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createRepayment(@RequestBody String orderId) {
            String paymentUrl = paymentService.createRepayment(orderId);
            return ResponseEntity.ok(paymentUrl);
    }

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getSummary() {
        return ResponseEntity.ok(paymentService.getSummary());
    }
    @DeleteMapping("/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> cancelPayment() {
        paymentService.cancelPayment();
        return ResponseEntity.noContent().build();
    }

    @Async
    @PostMapping("/webhook")
    public void webhook(@RequestBody String payload,
                                     @RequestHeader("Stripe-Signature") String signature) {
        try {
            paymentService.webhook(payload, signature);
        } catch (Exception e) {
            log.info("Error during webhook: {}", e.getMessage());
        }
    }
}
