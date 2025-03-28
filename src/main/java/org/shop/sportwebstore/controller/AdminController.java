package org.shop.sportwebstore.controller;

import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.model.ErrorResponse;
import org.shop.sportwebstore.model.dto.ProductDto;
import org.shop.sportwebstore.service.store.OrderService;
import org.shop.sportwebstore.service.store.ProductService;
import org.shop.sportwebstore.service.user.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;

    @GetMapping("/users")
    public ResponseEntity<?> getUser(@RequestParam(value = "page", defaultValue = "0") int page,
                                     @RequestParam(value = "search", defaultValue = "", required = false) String search,
                                     @RequestParam(value = "role", defaultValue = "", required = false) String role,
                                     @RequestParam(value = "enabled", defaultValue = "", required = false) String enabled){
        try {
            return ResponseEntity.ok(Map.of("users", userService.getAllUsers(page, search, role, enabled)));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error fetching users: " + e.getMessage()));
        }
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<?> changeUserStatus(@PathVariable String id, @RequestBody boolean status){
        try {
            userService.changeUserStatus(id, status);
            return ResponseEntity.ok(Map.of("message", "User status changed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error changing user status: " + e.getMessage()));
        }
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable String id, @RequestBody String role){
        try {
            userService.changeUserRole(id, role);
            return ResponseEntity.ok(Map.of("message", "User role changed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error changing user role: " + e.getMessage()));
        }
    }

    @GetMapping("/products")
    public ResponseEntity<?> getProducts(@RequestParam(value = "page", defaultValue = "0") int page,
                                     @RequestParam(value = "size", defaultValue = "10") int size,
                                     @RequestParam(value = "sort", defaultValue = "id") String sort,
                                     @RequestParam(value = "direction", defaultValue = "asc") String direction,
                                     @RequestParam(value = "search", defaultValue = "") String search,
                                     @RequestParam(value = "minPrice", defaultValue = "0") int minPrice,
                                     @RequestParam(value = "maxPrice", defaultValue = "9999") int maxPrice,
                                     @RequestParam(value = "categories", defaultValue = "", required = false) List<String> categories){
        try {
            return ResponseEntity.ok(productService.getProducts(page, size, sort, direction, search, minPrice, maxPrice, categories, true));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error fetching products: " + e.getMessage()));
        }
    }

    @PatchMapping("/products/{id}")
    public ResponseEntity<?> changeProductData(@PathVariable String id, @RequestBody ProductDto product) {
        try {
            return ResponseEntity.ok(Map.of("product", productService.changeProductData(id, product)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error changing product data: " + e.getMessage()));
        }
    }

    @PatchMapping("/products/{id}/available")
    public ResponseEntity<?> changeProductAvailability(@PathVariable String id, @RequestBody boolean available) {
        try {
            return ResponseEntity.ok(Map.of("product", productService.changeProductAvailability(id, available)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error changing product availability: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProduct(@RequestPart("product") String productJson,
                                        @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            return new ResponseEntity<>(Map.of("product", productService.addProduct(productJson, file)), CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error adding product: " + e.getMessage()));
        }
    }

    @PostMapping("/categories")
    public ResponseEntity<?> addCategory(@RequestBody String category) {
        try {
            return new ResponseEntity<>(Map.of("category", productService.addCategory(category)), CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error adding category: " + e.getMessage()));
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(@RequestParam(value = "page", defaultValue = "0") int page,
                                       @RequestParam(value = "size", defaultValue = "10") int size,
                                       @RequestParam(value = "status", defaultValue = "") String status){
        try {
            return ResponseEntity.ok(orderService.getOrders(page, size, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error fetching orders: " + e.getMessage()));
        }
    }

    @PatchMapping("/orders/{id}")
    public ResponseEntity<?> changeOrderStatus(@PathVariable String id){
        try {
            orderService.cancelOrder(id, true);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error changing order status: " + e.getMessage()));
        }
    }
}
