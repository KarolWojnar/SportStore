package org.shop.sportwebstore.controller;

import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.model.dto.OrderDto;
import org.shop.sportwebstore.service.store.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true", allowedHeaders = "*", exposedHeaders = "Authorization")
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public String createPayment(@RequestBody OrderDto orderDto) {
        try {
            return paymentService.createPayment(orderDto);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary() {
        try {
            return ResponseEntity.ok(Map.of("order", paymentService.getSummary()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
