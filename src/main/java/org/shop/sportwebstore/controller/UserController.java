package org.shop.sportwebstore.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.exception.ShopException;
import org.shop.sportwebstore.exception.UserException;
import org.shop.sportwebstore.model.ErrorResponse;
import org.shop.sportwebstore.model.dto.CustomerDto;
import org.shop.sportwebstore.model.dto.UserDto;
import org.shop.sportwebstore.service.user.UserService;
import org.shop.sportwebstore.service.ValidationUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto user, BindingResult br){
        if (br.hasErrors()) {
            return ResponseEntity.badRequest().body(ValidationUtil.buildValidationErrors(br));
        }
        try {
            return new ResponseEntity<>(Map.of("user", userService.createUser(user)), HttpStatus.CREATED);
        } catch (ShopException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Validation failed", Map.of("email", e.getMessage())));
        }
    }

    @PutMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateCustomer(@RequestBody CustomerDto user) {
        try {
            return ResponseEntity.ok(Map.of("user", userService.updateUser(user)));
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUser() {
        try {
            return ResponseEntity.ok(Map.of("user", userService.getUserDetails()));
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }


    @GetMapping("/theme")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getTheme() {
        try {
            return ResponseEntity.ok(Map.of("isDarkMode", userService.getTheme()));
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/theme")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> saveTheme(@RequestBody boolean isDarkMode) {
        try {
            return ResponseEntity.ok(Map.of("isDarkMode", userService.saveTheme(isDarkMode)));
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}
