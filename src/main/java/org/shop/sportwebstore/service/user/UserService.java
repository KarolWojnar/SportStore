package org.shop.sportwebstore.service.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shop.sportwebstore.exception.UserException;
import org.shop.sportwebstore.model.ActivationType;
import org.shop.sportwebstore.model.dto.AuthUser;
import org.shop.sportwebstore.model.dto.OrderDto;
import org.shop.sportwebstore.model.dto.ResetPassword;
import org.shop.sportwebstore.model.dto.UserDto;
import org.shop.sportwebstore.model.entity.Activation;
import org.shop.sportwebstore.model.entity.Cart;
import org.shop.sportwebstore.model.entity.Customer;
import org.shop.sportwebstore.model.entity.User;
import org.shop.sportwebstore.repository.ActivationRepository;
import org.shop.sportwebstore.repository.CustomerRepository;
import org.shop.sportwebstore.repository.UserRepository;
import org.shop.sportwebstore.service.store.CartService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final CartService cartService;
    private final ActivationRepository activationRepository;
    private final RedisTemplate<String, String> redisTemplate;
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
        return ResponseEntity.ok(Map.of("user", users.stream().map(UserDto::toUserDto).toList()));
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

    public Map<String, Object> login(AuthUser authRequest, HttpServletResponse response) {
        String token = getAuth(authRequest);
        String refreshToken = jwtUtil.generateToken(authRequest.getEmail(), refreshExp);

        Cookie refreshTokenCookie = new Cookie("Refresh-token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);
        boolean cartNotEmpty = isCartNotEmpty(authRequest.getEmail());
        return Map.of("token", token, "cartHasItems", cartNotEmpty);
    }

    private boolean isCartNotEmpty(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserException("User not found."));
        Cart cart = cartService.getCart(user.getId());
        return cart != null && !cart.getProducts().isEmpty();
    }

    public void logout(HttpServletResponse response, HttpServletRequest request) {
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
    }

    @Transactional
    public Map<String, String> activate(String activationCode) {
        Activation activation = activationRepository.findByActivationCodeAndTypeAndExpiresAtAfter(activationCode, ActivationType.REGISTRATION, LocalDateTime.now())
                .orElseThrow(() -> new UserException("Activation code not found."));
        User user = userRepository.findById(activation.getUserId())
                .orElseThrow(() -> new UserException("User not found."));
        user.setEnabled(true);
        userRepository.save(user);
        activationRepository.delete(activation);
        log.info("Account {} activated successfully", user.getId());
        return Map.of("email", user.getEmail());
    }

    public boolean saveTheme(boolean isDarkMode) {
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new UserException("User not found."));
        redisTemplate.opsForValue().set("theme:" + user.getId(), String.valueOf(isDarkMode));
        return isDarkMode;
    }

    public boolean getTheme() {
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new UserException("User not found."));

        String value = redisTemplate.opsForValue().get("theme:" + user.getId());
        return Boolean.parseBoolean(value);
    }

    public boolean isLoggedIn() {
        try {
            Optional<User> user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
            return user.isPresent() && user.get().isEnabled();
        } catch (Exception e) {
            throw new UserException("User not found.", e);
        }
    }

    public String getRole() {
        try {
            User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                    .orElseThrow(() -> new UserException("User not found."));
            return user.getRole().name();
        } catch (Exception e) {
            throw new UserException("User not found.", e);
        }
    }

    public String recoveryPassword(String email) {
        User user = userRepository.findByEmailAndEnabled(email, true).orElseThrow(() -> new UserException("Email not found."));
        Activation activation = activationRepository.save(new Activation(user.getId(), ActivationType.RESET_PASSWORD));
        emailService.sendEmailResetPassword(email, activation);
        return "Code sent! Check your email.";
    }

    public boolean checkResetCode(String code) {
        Activation activation = activationRepository.findByActivationCodeAndTypeAndExpiresAtAfter(code, ActivationType.RESET_PASSWORD, LocalDateTime.now())
                .orElseThrow(() -> new UserException("Activation code not found."));
        return activation.getActivationCode().equals(code);
    }

    @Transactional
    public String resetPassword(ResetPassword resetPassword) {
        if (!resetPassword.getPassword().equals(resetPassword.getConfirmPassword())) {
            throw new UserException("Passwords do not match.");
        }
        Activation activation = activationRepository.findByActivationCodeAndTypeAndExpiresAtAfter(resetPassword.getCode(), ActivationType.RESET_PASSWORD, LocalDateTime.now())
                .orElseThrow(() -> new UserException("Activation code not found."));
        User user = userRepository.findById(activation.getUserId())
                .orElseThrow(() -> new UserException("User not found."));
        user.setPassword(passwordEncoder.encode(resetPassword.getPassword()));
        userRepository.save(user);
        activationRepository.delete(activation);
        return "Password reset successfully.";
    }

    public Customer findOrCreateCustomer(OrderDto orderDto) {
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new UserException("User not found."));
        Customer customer = customerRepository.findByUserId(user.getId()).orElse(null);
        if (customer == null) {
            customer = new Customer();
        }
        customer.setUserId(user.getId());
        customer.setFirstName(orderDto.getFirstName());
        customer.setLastName(orderDto.getLastName());
        customer.setShippingAddress(orderDto.getShippingAddress());
        customerRepository.save(customer);
        return customer;
    }
}
