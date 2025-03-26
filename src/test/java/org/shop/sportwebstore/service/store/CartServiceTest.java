package org.shop.sportwebstore.service.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shop.sportwebstore.exception.ProductException;
import org.shop.sportwebstore.model.entity.Cart;
import org.shop.sportwebstore.model.entity.Product;
import org.shop.sportwebstore.model.entity.User;
import org.shop.sportwebstore.repository.ProductRepository;
import org.shop.sportwebstore.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private RedisTemplate<String, Cart> redisCartTemplate;

    @Mock
    private ValueOperations<String, Cart> valueOperations;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CartService cartService;

    private final String userId = "user123";
    private final String productId1 = "prod1";
    private final String userEmail = "user@example.com";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getCart_ReturnsCartWhenExists() {
        when(redisCartTemplate.opsForValue()).thenReturn(valueOperations);
        Cart expectedCart = new Cart(userId);
        when(valueOperations.get("cart:" + userId)).thenReturn(expectedCart);

        Cart result = cartService.getCart(userId);

        assertEquals(expectedCart, result);
    }

    @Test
    void getCart_ReturnsNullWhenNotExists() {
        when(redisCartTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("cart:" + userId)).thenReturn(null);

        Cart result = cartService.getCart(userId);

        assertNull(result);
    }

    @Test
    void saveCart_SavesCartSuccessfully() {
        when(redisCartTemplate.opsForValue()).thenReturn(valueOperations);
        Cart cart = new Cart(userId);

        cartService.saveCart(userId, cart);

        verify(valueOperations).set("cart:" + userId, cart);
    }

    @Test
    void deleteCart_DeletesCartSuccessfully() {
        cartService.deleteCart(userId);

        verify(redisCartTemplate).delete("cart:" + userId);
    }

    @Test
    void addToCart_AddsProductToNewCart() {
        when(redisCartTemplate.opsForValue()).thenReturn(valueOperations);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userEmail);

        Product product = new Product();
        product.setId(productId1);
        product.setAmountLeft(10);
        product.setAvailable(true);

        User user = new User();
        user.setId(userId);

        when(productRepository.findByIdAndAmountLeftIsGreaterThanAndAvailableTrue(productId1, 0))
                .thenReturn(Optional.of(product));
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(valueOperations.get("cart:" + userId)).thenReturn(null);

        cartService.addToCart(productId1);

        verify(valueOperations).set(eq("cart:" + userId), any(Cart.class));
    }

    @Test
    void addToCart_ThrowsExceptionWhenProductNotFound() {
        when(productRepository.findByIdAndAmountLeftIsGreaterThanAndAvailableTrue(productId1, 0))
                .thenReturn(Optional.empty());

        assertThrows(ProductException.class, () -> cartService.addToCart(productId1));
    }

    @Test
    void removeFromCart_RemovesProduct() {
        when(redisCartTemplate.opsForValue()).thenReturn(valueOperations);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userEmail);

        Cart cart = new Cart(userId);
        cart.addProduct(productId1, 2);

        User user = new User();
        user.setId(userId);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(valueOperations.get("cart:" + userId)).thenReturn(cart);

        cartService.removeFromCart(productId1);

        assertEquals(1, cart.getProducts().get(productId1));
        verify(valueOperations).set("cart:" + userId, cart);
    }

    @Test
    void deleteAllFromProduct_RemovesAllQuantity() {
        when(redisCartTemplate.opsForValue()).thenReturn(valueOperations);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userEmail);

        Cart cart = new Cart(userId);
        cart.addProduct(productId1, 3);

        User user = new User();
        user.setId(userId);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(valueOperations.get("cart:" + userId)).thenReturn(cart);

        cartService.deleteAllFromProduct(productId1);

        assertFalse(cart.getProducts().containsKey(productId1));
        verify(valueOperations).set("cart:" + userId, cart);
    }

    @Test
    void checkCartProducts_ThrowsExceptionWhenCartEmpty() {
        Cart emptyCart = new Cart(userId);

        assertThrows(ProductException.class, () -> cartService.checkCartProducts(emptyCart, false));
    }

    @Test
    void checkCartProducts_ThrowsExceptionWhenOrderProcessing() {
        Cart cart = new Cart(userId);
        cart.addProduct(productId1, 1);
        cart.setOrderProcessing(true);

        assertThrows(ProductException.class, () -> cartService.checkCartProducts(cart, false));
    }

    @Test
    void checkCartProducts_ThrowsExceptionWhenNotEnoughStock() {
        Cart cart = new Cart(userId);
        cart.addProduct(productId1, 5);

        Product product = new Product();
        product.setId(productId1);
        product.setAmountLeft(2);

        when(productRepository.findAllById(Set.of(productId1))).thenReturn(List.of(product));

        assertThrows(ProductException.class, () -> cartService.checkCartProducts(cart, false));
    }

    @Test
    void calculateTotalPrice_ReturnsCorrectTotal() {
        Cart cart = new Cart(userId);
        cart.addProduct(productId1, 2);
        String productId2 = "prod2";
        cart.addProduct(productId2, 1);

        Product product1 = new Product();
        product1.setId(productId1);
        product1.setPrice(10.0);

        Product product2 = new Product();
        product2.setId(productId2);
        product2.setPrice(5.0);

        when(productRepository.findAllById(Set.of(productId1, productId2))).thenReturn(List.of(product1, product2));

        double total = cartService.calculateTotalPrice(cart);

        assertEquals(25.0, total);
    }

    @Test
    void getCart_ReturnsEmptyMapWhenCartNotExists() {
        when(redisCartTemplate.opsForValue()).thenReturn(valueOperations);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userEmail);

        User user = new User();
        user.setId(userId);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(valueOperations.get("cart:" + userId)).thenReturn(null);

        Map<String, Object> result = cartService.getCart();

        assertEquals(List.of(), result.get("products"));
    }

    @Test
    void cancelPayment_ReleasesBlockedProducts() {
        when(redisCartTemplate.opsForValue()).thenReturn(valueOperations);
        Cart cart = new Cart(userId);
        cart.addProduct(productId1, 2);
        cart.setOrderProcessing(true);

        Product product = new Product();
        product.setId(productId1);
        product.setAmountLeft(5);

        cartService.cancelPayment(cart, List.of(product));

        assertEquals(7, product.getAmountLeft());
        assertFalse(cart.isOrderProcessing());
        verify(valueOperations).set("cart:" + userId, cart);
    }

    @Test
    void findCartsByIsOrderProcessing_ReturnsExpiredCarts() {
        when(redisCartTemplate.opsForValue()).thenReturn(valueOperations);
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() - 3600000);

        Cart expiredCart = new Cart(userId);
        expiredCart.setOrderProcessing(true);
        expiredCart.setLastModified(expiredDate);

        when(redisCartTemplate.keys("cart:*")).thenReturn(Set.of("cart:1"));
        when(valueOperations.get("cart:1")).thenReturn(expiredCart);

        List<Cart> result = cartService.findCartsByIsOrderProcessing(true, now);

        assertEquals(1, result.size());
        assertEquals(expiredCart, result.get(0));
    }
}