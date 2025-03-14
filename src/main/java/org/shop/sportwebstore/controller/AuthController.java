package org.shop.sportwebstore.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.exception.UserException;
import org.shop.sportwebstore.model.ErrorResponse;
import org.shop.sportwebstore.model.dto.AuthUser;
import org.shop.sportwebstore.model.dto.ResetPassword;
import org.shop.sportwebstore.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true", allowedHeaders = "*", exposedHeaders = "Authorization")
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthUser authRequest, HttpServletResponse response) {
        try {
            return ResponseEntity.ok(userService.login(authRequest, response));
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/recovery-password")
    public ResponseEntity<?> recoveryPassword(@RequestBody String email) {
        try {
            return ResponseEntity.ok(Map.of("message", userService.recoveryPassword(email)));
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }



    @GetMapping("/check-reset-code/{code}")
    public ResponseEntity<?> checkResetCode(@PathVariable String code) {
        try {
            return ResponseEntity.ok(userService.checkResetCode(code));
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPassword resetPassword) {
        try {
            log.info("Reset password for user: {}", resetPassword);
            return ResponseEntity.ok(Map.of("message", userService.resetPassword(resetPassword)));
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout(HttpServletResponse response, HttpServletRequest request) {
        try {
            userService.logout(response, request);
            return ResponseEntity.ok().build();
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/activate/{activationCode}")
    public ResponseEntity<?> activate(@PathVariable String activationCode) {
        try {
            return ResponseEntity.ok(userService.activate(activationCode));
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/isLoggedIn")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> isLoggedIn() {
        try {
            return ResponseEntity.ok(Map.of("isLoggedIn", userService.isLoggedIn()));
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/role")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getRole() {
        try {
            return ResponseEntity.ok(Map.of("role", userService.getRole()));
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}
