package org.shop.sportwebstore.service.store;

import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.exception.ProductException;
import org.shop.sportwebstore.model.entity.Cart;
import org.shop.sportwebstore.model.entity.Product;
import org.shop.sportwebstore.repository.ProductRepository;
import org.shop.sportwebstore.service.ConstantStrings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final RedisTemplate<String, Cart> redisCartTemplate;
    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;

    @Transactional(rollbackFor = ProductException.class)
    public void checkCartProducts(Cart cart, boolean isPayment) {
        if (cart == null || cart.getProducts().isEmpty()) {
            throw new ProductException("Cart is empty.");
        }
        if (cart.isOrderProcessing()) {
            throw new ProductException("Order is already processing.");
        }
        List<Product> products = productRepository.findAllById(cart.getProducts().keySet());
        for (Product product : products) {
            if (product.getAmountLeft() < cart.getProducts().get(product.getId())) {
                throw new ProductException("Not enough products in stock.");
            }
            if (isPayment) {
                blockAmountItem(product.getId(), cart.getProducts().get(product.getId()));
            }
        }
        if (isPayment) {
            cart.setOrderProcessing(true);
            redisCartTemplate.opsForValue().set("cart:" + cart.getUserId(), cart);
        }
    }

    private void blockAmountItem(String productId, int amountOfCart) {
        Query query = new Query(Criteria.where("_id").is(productId)
                        .and("amountLeft").gte(amountOfCart));
        Update update = new Update().inc("amountLeft", -amountOfCart);
        Product product = mongoTemplate.findAndModify(query, update, Product.class);
        if (product == null) {
            throw new ProductException("Could not block product. Not enough available items.");
        }
    }

    public Cart getCart(String userId) {
        return redisCartTemplate.opsForValue().get("cart:" + userId);
    }

    public void saveCart(String userId, Cart cart) {
        redisCartTemplate.opsForValue().set("cart:" + userId, cart);
    }

    public void deleteCart(String userId) {
        redisCartTemplate.delete("cart:" + userId);
    }

    @Transactional
    public void cancelPayment(Cart cart, List<Product> products) {
        for (Product product : products) {
            product.setAmountLeft(product.getAmountLeft() + cart.getProducts().get(product.getId()));
            productRepository.save(product);
        }
        cart.setOrderProcessing(false);
        redisCartTemplate.opsForValue().set("cart:" + cart.getUserId(), cart);
    }

    public List<Cart> findCartsByIsOrderProcessing(boolean isOrderProcessing, Date now) {
        long expirationTimeInMillis = now.getTime() - ConstantStrings.ORDER_EXPIRATION.toMillis();
        Date expirationDate = new Date(expirationTimeInMillis);
        Set<String> cartKeys = redisCartTemplate.keys("cart:*");

        return cartKeys.stream()
                .map(key -> redisCartTemplate.opsForValue().get(key))
                .filter(cart -> cart != null)
                .filter(cart -> cart.isOrderProcessing() == isOrderProcessing
                        && cart.getLastModified().before(expirationDate))
                .collect(Collectors.toList());
    }
}
