package org.shop.sportwebstore.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.exception.UserException;
import org.shop.sportwebstore.model.ErrorResponse;
import org.shop.sportwebstore.model.dto.AuthUser;
import org.shop.sportwebstore.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthUser authRequest, HttpServletResponse response) {
        try {
            return ResponseEntity.ok(userService.login(authRequest, response));
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> refreshToken(@RequestBody AuthUser authRequest) {
        try {
            return ResponseEntity.ok(userService.refreshToken(authRequest));
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout(HttpServletResponse response, HttpServletRequest request) {
        try {
            return ResponseEntity.ok(userService.logout(response, request));
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}
