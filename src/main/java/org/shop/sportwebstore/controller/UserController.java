package org.shop.sportwebstore.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.model.dto.CustomerDto;
import org.shop.sportwebstore.model.dto.UserDto;
import org.shop.sportwebstore.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto user) {
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }

    @PutMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateCustomer(@RequestBody CustomerDto user) {
        return ResponseEntity.ok(userService.updateUser(user));
    }

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUser() {
        return ResponseEntity.ok(userService.getUserDetails());
    }

}
