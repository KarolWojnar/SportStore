package org.shop.sportwebstore.controller;

import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.model.ErrorResponse;
import org.shop.sportwebstore.model.dto.RateProductDto;
import org.shop.sportwebstore.service.store.CartService;
import org.shop.sportwebstore.service.store.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true", allowedHeaders = "*", exposedHeaders = "Authorization")
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
                                         @RequestParam(value = "minPrice", defaultValue = "0") int minPrice,
                                         @RequestParam(value = "maxPrice", defaultValue = "9999") int maxPrice,
                                         @RequestParam(value = "categories", defaultValue = "", required = false) List<String> categories) {
        try {
            return ResponseEntity.ok(productService.getProducts(page, size, sort, direction, search, minPrice, maxPrice, categories, false));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/max-price")
    public ResponseEntity<?> getMaxPrice() {
        try {
            return ResponseEntity.ok(Map.of("maxPrice", productService.getMaxPrice()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok(Map.of("categories", productService.getCategories()));
    }

    @GetMapping("/featured")
    public ResponseEntity<?> getFeaturedProducts() {
        try {
            return ResponseEntity.ok(productService.getFeaturedProducts());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductDetails(@PathVariable String id) {
        try {
            return ResponseEntity.ok(productService.getDetails(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/cart")
    public ResponseEntity<?> getCart() {
        try {
            return ResponseEntity.ok(cartService.getCart());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cart/add")
    public ResponseEntity<?> addToCart(@RequestBody String productId) {
        try {
            cartService.addToCart(productId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cart/remove")
    public ResponseEntity<?> removeFromCart(@RequestBody String id) {
        try {
            cartService.removeFromCart(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/cart/{id}")
    public ResponseEntity<?> deleteAllFromProduct(@PathVariable String id) {
        try {
            cartService.deleteAllFromProduct(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/cart")
    public ResponseEntity<?> deleteCart() {
        try {
            cartService.deleteCart();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/cart/valid")
    public ResponseEntity<?> validateCart() {
        try {
            cartService.validateCart();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/cart/totalPrice")
    public ResponseEntity<?> calculateTotalPrice() {
        try {
            return ResponseEntity.ok(Map.of("totalPrice", cartService.calculateTotalPriceOfCart()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/rate")
    public ResponseEntity<?> rateProduct(@RequestBody RateProductDto rateProductDto) {
        try {
            productService.rateProduct(rateProductDto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}
