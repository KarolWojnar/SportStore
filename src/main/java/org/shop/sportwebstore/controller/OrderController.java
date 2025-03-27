package org.shop.sportwebstore.controller;

import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.model.ErrorResponse;
import org.shop.sportwebstore.service.store.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<?> getAllOrdersByUser() {
        try {
            return ResponseEntity.ok(Map.of("orders", orderService.getUserOrders()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(Map.of("order", orderService.getOrderById(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PatchMapping("/cancel/{id}")
    public ResponseEntity<?> cancelOrder(@PathVariable String id) {
        try {
            orderService.cancelOrder(id, false);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PatchMapping("/refund/{id}")
    public ResponseEntity<?> refundOrder(@PathVariable String id) {
        try {
            orderService.refundOrder(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}
