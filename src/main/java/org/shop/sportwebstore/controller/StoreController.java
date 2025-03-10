package org.shop.sportwebstore.controller;

import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.model.ErrorResponse;
import org.shop.sportwebstore.service.store.StoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true", allowedHeaders = "*")
@RequestMapping("/api/store")
public class StoreController {

    private final StoreService storeService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/add-to-cart")
    public ResponseEntity<?> addToCart(String productId) {
        try {
            storeService.addToCart(productId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getProducts(@RequestParam(value = "page", defaultValue = "0") int page,
                                         @RequestParam(value = "size", defaultValue = "6") int size,
                                         @RequestParam(value = "sort", defaultValue = "id") String sort,
                                         @RequestParam(value = "direction", defaultValue = "asc") String direction,
                                         @RequestParam(value = "search", defaultValue = "") String search,
                                         @RequestParam(value = "categories", defaultValue = "", required = false) List<String> categories) {
        try {
            return ResponseEntity.ok(storeService.getProducts(page, size, sort, direction, search, categories));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok(storeService.getCategories());
    }
}
