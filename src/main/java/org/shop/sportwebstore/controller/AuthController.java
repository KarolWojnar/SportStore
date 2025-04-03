package org.shop.sportwebstore.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.model.SuccessResponse;
import org.shop.sportwebstore.model.dto.AuthUser;
import org.shop.sportwebstore.model.dto.LoginStatus;
import org.shop.sportwebstore.model.dto.ResetPassword;
import org.shop.sportwebstore.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthUser authRequest, HttpServletResponse response) {
        return ResponseEntity.ok(userService.login(authRequest, response));
    }

    @PostMapping("/recovery-password")
    public ResponseEntity<?> recoveryPassword(@RequestBody String email) {
        return ResponseEntity.ok(new SuccessResponse(userService.recoveryPassword(email)));
    }

    @GetMapping("/check-reset-code/{code}")
    public ResponseEntity<?> checkResetCode(@PathVariable String code) {
        return ResponseEntity.ok(userService.checkResetCode(code));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPassword resetPassword) {
        return ResponseEntity.ok(new SuccessResponse(userService.resetPassword(resetPassword)));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout(HttpServletResponse response, HttpServletRequest request) {
        userService.logout(response, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/activate/{activationCode}")
    public ResponseEntity<?> activate(@PathVariable String activationCode) {
        return ResponseEntity.ok(userService.activate(activationCode));
    }

    @GetMapping("/isLoggedIn")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> isLoggedIn() {
        return ResponseEntity.ok(new LoginStatus(userService.isLoggedIn()));
    }

    @GetMapping("/role")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getRole() {
        return ResponseEntity.ok(new LoginStatus(userService.getRole()));
    }
}
