package org.shop.sportwebstore.service.store;

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

    public void deleteCart(String userId) {
        redisTemplate.delete("cart:" + userId);
    }
}
