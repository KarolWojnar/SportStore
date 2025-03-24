package org.shop.sportwebstore.controller;

import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.model.ErrorResponse;
import org.shop.sportwebstore.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true", allowedHeaders = "*", exposedHeaders = "Authorization")
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users/{page}")
    public ResponseEntity<?> getUser(@PathVariable int page){
        try {
            return ResponseEntity.ok(Map.of("users", userService.getAllUsers(page)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error fetching users: " + e.getMessage()));
        }
    }
}
