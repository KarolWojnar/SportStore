package org.shop.sportwebstore.controller;

import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true", allowedHeaders = "*", exposedHeaders = "Authorization")
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUser(){
        return userService.finAllUsers();
    }
}
