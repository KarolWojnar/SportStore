package org.shop.sportwebstore.service.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shop.sportwebstore.exception.UserException;
import org.shop.sportwebstore.model.ActivationType;
import org.shop.sportwebstore.model.dto.AuthUser;
import org.shop.sportwebstore.model.dto.UserDto;
import org.shop.sportwebstore.model.entity.Activation;
import org.shop.sportwebstore.model.entity.Cart;
import org.shop.sportwebstore.model.entity.User;
import org.shop.sportwebstore.repository.ActivationRepository;
import org.shop.sportwebstore.repository.CustomerRepository;
import org.shop.sportwebstore.repository.UserRepository;
import org.shop.sportwebstore.service.store.CartRedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Service
@Validated
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CartRedisService cartRedisService;
    private final ActivationRepository activationRepository;
    @Value("${jwt.exp}")
    private int exp;
    @Value("${jwt.refresh.exp}")
    private int refreshExp;

    @Transactional
    public UserDto createUser(UserDto user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserException("Email already exists.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User newUser = userRepository.save(UserDto.toUserEntity(user));
        Activation activation = activationRepository.save(new Activation(newUser.getId(), ActivationType.REGISTRATION));
        emailService.sendEmailActivation(user.getEmail(), activation);
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

    public Map<String, Object> login(AuthUser authRequest, HttpServletResponse response) {
        String token = getAuth(authRequest);
        String refreshToken = jwtUtil.generateToken(authRequest.getEmail(), refreshExp);

        Cookie refreshTokenCookie = new Cookie("Refresh-token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);
        response.setHeader("X-Cart-Has-Items", String.valueOf(isCartNotEmpty(authRequest.getEmail())));
        return Map.of("token", token);
    }

    private boolean isCartNotEmpty(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserException("User not found."));
        Cart cart = cartRedisService.getCart(user.getId());
        return cart != null && !cart.getProducts().isEmpty();
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

    @Transactional
    public Map<String, String> activate(String activationCode) {
        Activation activation = activationRepository.findByActivationCodeAndType(activationCode, ActivationType.REGISTRATION)
                .orElseThrow(() -> new UserException("Activation code not found."));
        User user = userRepository.findById(activation.getUserId())
                .orElseThrow(() -> new UserException("User not found."));
        user.setEnabled(true);
        userRepository.save(user);
        activationRepository.delete(activation);
        log.info("Account {} activated successfully", user.getId());
        return Map.of("email", user.getEmail());
    }
}
