package org.shop.sportwebstore.service.store;

import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.exception.ProductException;
import org.shop.sportwebstore.model.dto.ProductCart;
import org.shop.sportwebstore.model.entity.Cart;
import org.shop.sportwebstore.model.entity.Product;
import org.shop.sportwebstore.model.entity.User;
import org.shop.sportwebstore.repository.ProductRepository;
import org.shop.sportwebstore.repository.UserRepository;
import org.shop.sportwebstore.service.ConstantStrings;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final RedisTemplate<String, Cart> redisCartTemplate;
    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;


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

    public void addToCart(String productId) {
        Product product = productRepository.findByIdAndAmountLeftIsGreaterThan(productId, 0).orElseThrow(() -> new ProductException("Product not found."));
        String authUser = SecurityContextHolder.getContext().getAuthentication().getName();
        String userId = userRepository.findByEmail(authUser).orElseThrow().getId();
        Cart cart = getCart(userId);
        if (cart == null) {
            cart = new Cart(userId);
        }
        if (checkAmount(cart, product)) {
            cart.addProduct(productId, 1);
            saveCart(userId, cart);
        }
    }

    private boolean checkAmount(Cart cart, Product product) {
        return product.getAmountLeft() >= cart.getQuantity(product.getId()) + 1;
    }

    private void blockAmountItem(String productId, int amountOfCart) {
        Query query = new Query(Criteria.where("_id").is(productId)
                        .and("amountLeft").gte(amountOfCart));
        Update update = new Update().inc("amountLeft", -amountOfCart);
        mongoTemplate.findAndModify(query, update, Product.class);
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
                .filter(cart -> cart.isOrderProcessing() == isOrderProcessing
                        && cart.getLastModified().before(expirationDate))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getCart() {
        String authUser = SecurityContextHolder.getContext().getAuthentication().getName();
        String userId = userRepository.findByEmail(authUser).orElseThrow().getId();
        Cart cart = getCart(userId);
        if (cart == null) {
            return Map.of("products", List.of());
        }
        List<Product> products = productRepository.findAllById(cart.getProducts().keySet());
        List<ProductCart> productCarts = products.stream().map(product -> ProductCart.toDto(product, cart.getProducts().get(product.getId()))).toList();
        return Map.of("products", productCarts);
    }

    public void removeFromCart(String id) {
        String authUser = SecurityContextHolder.getContext().getAuthentication().getName();
        String userId = userRepository.findByEmail(authUser).orElseThrow().getId();
        Cart cart = getCart(userId);
        if (cart == null) {
            throw new ProductException("Cart is empty.");
        }
        cart.removeProduct(id);
        saveCart(userId, cart);
    }

    public void deleteAllFromProduct(String id) {
        String authUser = SecurityContextHolder.getContext().getAuthentication().getName();
        String userId = userRepository.findByEmail(authUser).orElseThrow().getId();
        Cart cart = getCart(userId);
        if (cart == null) {
            throw new ProductException("Cart is empty.");
        }
        cart.getProducts().remove(id);
        saveCart(userId, cart);
    }

    public void deleteCart() {
        String authUser = SecurityContextHolder.getContext().getAuthentication().getName();
        String userId = userRepository.findByEmail(authUser).orElseThrow().getId();
        deleteCart(userId);
    }

    public void validateCart() {
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("User not found."));
        Cart cart = getCart(user.getId());
        checkCartProducts(cart, false);
    }

    public double calculateTotalPriceOfCart() {
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("User not found."));
        Cart cart = getCart(user.getId());
        if (cart == null) {
            throw new ProductException("Cart is empty.");
        }
        return calculateTotalPrice(cart);
    }

    public double calculateTotalPrice(Cart cart) {
        List<Product> products = productRepository.findAllById(cart.getProducts().keySet());
        double totalPrice = 0;
        for (Product product : products) {
            totalPrice += product.getPrice() * cart.getProducts().get(product.getId());
        }
        return totalPrice;
    }
}
