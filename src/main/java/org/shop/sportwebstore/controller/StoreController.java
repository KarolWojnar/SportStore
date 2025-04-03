package org.shop.sportwebstore.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.model.dto.RateProductDto;
import org.shop.sportwebstore.service.store.CartService;
import org.shop.sportwebstore.service.store.ProductService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store")
public class StoreController {

    private final ProductService productService;
    private final CartService cartService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProducts(@RequestParam(value = "page", defaultValue = "0") int page,
                                         @RequestParam(value = "size", defaultValue = "6") int size,
                                         @RequestParam(value = "sort", defaultValue = "id") String sort,
                                         @RequestParam(value = "direction", defaultValue = "asc") String direction,
                                         @RequestParam(value = "search", defaultValue = "") String search,
                                         @RequestParam(value = "minPrice", defaultValue = "0") @Min(0) int minPrice,
                                         @RequestParam(value = "maxPrice", defaultValue = "9999") @Max(9999) int maxPrice,
                                         @RequestParam(value = "categories", defaultValue = "", required = false) List<String> categories) {
        return ResponseEntity
                .ok(productService
                        .getProducts(page, size, sort, direction, search, minPrice, maxPrice, categories, false));
    }

    @Cacheable("maxPrice")
    @GetMapping("/max-price")
    public ResponseEntity<?> getMaxPrice() {
        return ResponseEntity.ok(Map.of("maxPrice", productService.getMaxPrice()));
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok(productService.getCategories());
    }

    @GetMapping("/featured")
    public ResponseEntity<?> getFeaturedProducts() {
        return ResponseEntity.ok(productService.getFeaturedProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductDetails(@PathVariable String id) {
        return ResponseEntity.ok(productService.getDetails(id));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/cart")
    public ResponseEntity<?> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cart/add")
    public ResponseEntity<?> addToCart(@RequestBody String productId) {
        cartService.addToCart(productId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cart/remove")
    public ResponseEntity<?> removeFromCart(@RequestBody String id) {
        cartService.removeFromCart(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/cart/{id}")
    public ResponseEntity<?> deleteAllFromProduct(@PathVariable String id) {
        cartService.deleteAllFromProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/cart")
    public ResponseEntity<?> deleteCart() {
        cartService.deleteCart();
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/cart/valid")
    public ResponseEntity<?> validateCart() {
        cartService.validateCart();
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/rate")
    public ResponseEntity<?> rateProduct(@RequestBody RateProductDto rateProductDto) {
        productService.rateProduct(rateProductDto);
        return ResponseEntity.noContent().build();
    }
}
