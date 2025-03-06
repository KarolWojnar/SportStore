package org.shop.sportwebstore.service;

import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.exception.UserException;
import org.shop.sportwebstore.model.dto.UserDto;
import org.shop.sportwebstore.model.entity.Customer;
import org.shop.sportwebstore.model.entity.User;
import org.shop.sportwebstore.repository.CustomerRepository;
import org.shop.sportwebstore.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

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
            return UserDto.toCustomerDto(customerRepository.save(UserDto.toCustomerEntity(user, newUser)));
        }
        return UserDto.toUserDto(newUser);
    }

    public ResponseEntity<?> finAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users.stream().map(UserDto::toUserDto).toList());
    }

    public ResponseEntity<?> finAllCustomers() {
        List<Customer> users = customerRepository.findAll();
        return ResponseEntity.ok(users.stream().map(UserDto::toCustomerDto).toList());
    }
}
