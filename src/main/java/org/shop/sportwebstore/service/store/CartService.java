package org.shop.sportwebstore.service.store;

import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.exception.ProductException;
import org.shop.sportwebstore.model.entity.Cart;
import org.shop.sportwebstore.model.entity.Product;
import org.shop.sportwebstore.repository.ProductRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {


    private final RedisTemplate<String, Cart> redisTemplate;
    private final ProductRepository productRepository;

    @Transactional(rollbackFor = ProductException.class)
    public void checkCartProducts(Cart cart, boolean isPayment) {
        if (cart == null || cart.getProducts().isEmpty()) {
            throw new ProductException("Cart is empty.");
        }
        List<Product> products = productRepository.findAllById(cart.getProducts().keySet());
        for (Product product : products) {
            if (product.getAmountLeft() < cart.getProducts().get(product.getId())) {
                throw new ProductException("Not enough products in stock.");
            }
            blockAmountItem(product, cart.getProducts().get(product.getId()));
        }
    }

    private void blockAmountItem(Product product, int amountOfCart) {
        product.setAmountLeft(product.getAmountLeft() - amountOfCart);
        productRepository.save(product);
    }

    public Cart getCart(String userId) {
        return redisTemplate.opsForValue().get("cart:" + userId);
    }

    public void saveCart(String userId, Cart cart) {
        redisTemplate.opsForValue().set("cart:" + userId, cart);
    }

    public void deleteCart(String userId) {
        redisTemplate.delete("cart:" + userId);
    }

    @Transactional()
    public void cancelPayment(Cart cart, List<Product> products) {
        for (Product product : products) {
            product.setAmountLeft(product.getAmountLeft() + cart.getProducts().get(product.getId()));
            productRepository.save(product);
        }
    }
}
