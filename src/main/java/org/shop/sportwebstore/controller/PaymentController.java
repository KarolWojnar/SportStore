package org.shop.sportwebstore.controller;

import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.model.ErrorResponse;
import org.shop.sportwebstore.model.dto.OrderDto;
import org.shop.sportwebstore.service.store.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true", allowedHeaders = "*", exposedHeaders = "Authorization")
@RequestMapping("/api/payment")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPayment(@RequestBody OrderDto orderDto) {
        try {
            String paymentUrl = paymentService.createPayment(orderDto);
            return ResponseEntity.ok(Map.of("url", paymentUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary() {
        try {
            return ResponseEntity.ok(Map.of("order", paymentService.getSummary()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    @DeleteMapping("/cancel")
    public ResponseEntity<?> cancelPayment() {
        try {
            paymentService.cancelPayment();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(@RequestBody String payload,
                                     @RequestHeader("Stripe-Signature") String signature) {
        try {
            paymentService.webhook(payload, signature);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.info("Error during webhook: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
