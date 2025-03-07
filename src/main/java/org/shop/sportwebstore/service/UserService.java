package org.shop.sportwebstore.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shop.sportwebstore.exception.UserException;
import org.shop.sportwebstore.model.dto.AuthUser;
import org.shop.sportwebstore.model.dto.UserDto;
import org.shop.sportwebstore.model.entity.User;
import org.shop.sportwebstore.repository.CustomerRepository;
import org.shop.sportwebstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    @Value("${jwt.exp}")
    private int exp;
    @Value("${jwt.refresh.exp}")
    private int refreshExp;

    public UserDto createUser(UserDto user) {
        if (!user.getPassword().equals(user.getConfirmPassword())) {
            throw new UserException("Password and confirm password do not match.");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserException("Email already exists.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User newUser = userRepository.save(UserDto.toUserEntity(user));
        if (user.getFirstName() != null || user.getLastName() != null || user.getShippingAddress() != null) {
            return UserDto.toCustomerDto(customerRepository.save(UserDto.toCustomerEntity(user, newUser)), newUser);
        }
        return UserDto.toUserDto(newUser);
    }

    public ResponseEntity<?> finAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users.stream().map(UserDto::toUserDto).toList());
    }

    public String getAuth(AuthUser authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),
                            authRequest.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new UserException("Invalid email or password.", e);
        }
        return jwtUtil.generateToken(authRequest.getEmail(), exp);
    }

    public Object refreshToken(AuthUser authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),
                            authRequest.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new UserException("Invalid email or password.", e);
        }
        return jwtUtil.refreshToken(authRequest.getEmail(), refreshExp);
    }

    public String login(AuthUser authRequest, HttpServletResponse response) {
        String accessToken = getAuth(authRequest);
        String refreshToken = jwtUtil.generateToken(authRequest.getEmail(), refreshExp);

        Cookie refreshTokenCookie = new Cookie("Refresh-token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);

        return accessToken;
    }

    public String logout(HttpServletResponse response, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("Refresh-token".equals(cookie.getName())) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }

        SecurityContextHolder.clearContext();

        log.info("Logout successful");
        return "Logout successful";
    }
}
