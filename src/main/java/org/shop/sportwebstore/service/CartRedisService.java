package org.shop.sportwebstore.service;

import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.model.entity.Cart;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartRedisService {

    private final RedisTemplate<String, Cart> redisTemplate;

    public Cart getCart(String userId) {
        return redisTemplate.opsForValue().get("cart:" + userId);
    }

    public void saveCart(String userId, Cart cart) {
        redisTemplate.opsForValue().set("cart:" + userId, cart);
    }
}
