package org.shop.sportwebstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shop.sportwebstore.exception.ProductException;
import org.shop.sportwebstore.model.entity.Cart;
import org.shop.sportwebstore.repository.ProductRepository;
import org.shop.sportwebstore.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreService {

    private final CartRedisService cartRedisService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public void addToCart(String productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductException("Product is unavailable.");
        }
        String authUser = SecurityContextHolder.getContext().getAuthentication().getName();
        String userId = userRepository.findByEmail(authUser).orElseThrow().getId();
        Cart cart = cartRedisService.getCart(userId);
        if (cart == null) {
            cart = new Cart(userId);
        }
        cart.addProduct(productId, 1);
        cartRedisService.saveCart(userId, cart);
    }

}
