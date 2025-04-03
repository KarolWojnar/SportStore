package org.shop.sportwebstore.service.user;

import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.model.entity.User;
import org.shop.sportwebstore.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class SecurityContextWrapper {
    private final UserRepository userRepository;


    public Optional<User> getCurrentUser() {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            return userRepository.findByEmail(userEmail);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
