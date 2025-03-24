package org.shop.sportwebstore.controller;

import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.model.ErrorResponse;
import org.shop.sportwebstore.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true", allowedHeaders = "*", exposedHeaders = "Authorization")
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

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
}
