package org.shop.sportwebstore.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.exception.ShopException;
import org.shop.sportwebstore.model.ErrorResponse;
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
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true", allowedHeaders = "*")
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUser(){
        return userService.finAllUsers();
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto user, BindingResult br){
        if (br.hasErrors()) {
            return ResponseEntity.badRequest().body(ValidationUtil.buildValidationErrors(br));
        }
        try {
            return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
        } catch (ShopException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Validation failed", Map.of("email", e.getMessage())));
        }
    }
}
