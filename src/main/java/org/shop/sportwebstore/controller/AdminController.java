package org.shop.sportwebstore.controller;

import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.model.Roles;
import org.shop.sportwebstore.model.SuccessResponse;
import org.shop.sportwebstore.model.dto.*;
import org.shop.sportwebstore.service.store.OrderService;
import org.shop.sportwebstore.service.store.ProductService;
import org.shop.sportwebstore.service.user.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
        List<UserDetailsDto> users = userService.getAllUsers(page, search, role, enabled);
        return ResponseEntity.ok(new UsersResponse(users));
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<?> changeUserStatus(@PathVariable String id, @RequestBody UserStatusRequest status){
        userService.changeUserStatus(id, status);
        return ResponseEntity.ok(new SuccessResponse("User status changed"));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable String id, @RequestBody Roles role) {
        userService.changeUserRole(id, role);
        return ResponseEntity.ok(new SuccessResponse("User role changed"));
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
        return ResponseEntity.ok(productService.getProducts(page, size, sort, direction, search, minPrice, maxPrice, categories, true));
    }

    @PatchMapping("/products/{id}")
    public ResponseEntity<?> changeProductData(@PathVariable String id, @RequestBody ProductDto product) {
        return ResponseEntity.ok(productService.changeProductData(id, product));
    }

    @PatchMapping("/products/{id}/available")
    public ResponseEntity<?> changeProductAvailability(@PathVariable String id, @RequestBody ProductAvailability available) {
        return ResponseEntity.ok(productService.changeProductAvailability(id, available));
    }

    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProduct(@RequestPart("product") String productJson,
                                        @RequestPart(value = "file", required = false) MultipartFile file) {
        return new ResponseEntity<>(productService.addProduct(productJson, file), CREATED);
    }

    @PostMapping("/categories")
    public ResponseEntity<?> addCategory(@RequestBody CategoryDto category) {
        return new ResponseEntity<>(productService.addCategory(category), CREATED);
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(@RequestParam(value = "page", defaultValue = "0") int page,
                                       @RequestParam(value = "size", defaultValue = "10") int size,
                                       @RequestParam(value = "status", defaultValue = "") String status){
        return ResponseEntity.ok(orderService.getOrders(page, size, status));
    }

    @PatchMapping("/orders/{id}")
    public ResponseEntity<?> changeOrderStatus(@PathVariable String id) {
        orderService.cancelOrder(id, true);
        return ResponseEntity.noContent().build();
    }
}
