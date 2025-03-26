package org.shop.sportwebstore.service.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shop.sportwebstore.exception.ProductException;
import org.shop.sportwebstore.model.entity.Cart;
import org.shop.sportwebstore.repository.ProductRepository;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private Cart cart;

    @BeforeEach
    void setUp() {
        cart = new Cart("testUserId");
    }

    @Test
    public void checkCartProducts_throwsException() {
        ProductException exception = assertThrows(ProductException.class, () -> cartService.checkCartProducts(cart, false));
        assertEquals("Cart is empty.", exception.getMessage());
    }

    @Test
    public void checkCartProduct_alreadyProcessing() {
        cart.setProducts(Map.of("testProduct", 5));
        cart.setOrderProcessing(true);
        ProductException exception = assertThrows(ProductException.class, () -> cartService.checkCartProducts(cart, false));
        assertEquals("Order is already processing.", exception.getMessage());
    }

    @Test
    public void addToCart_throwsException_whenProductNotFound() {
        String nonExistingProductId = "nonExistingProduct";
        when(productRepository.findByIdAndAmountLeftIsGreaterThanAndAvailableTrue(nonExistingProductId, 0))
                .thenReturn(Optional.empty());

        ProductException exception = assertThrows(ProductException.class, () -> cartService.addToCart(nonExistingProductId));
        assertEquals("Product not found.", exception.getMessage());
    }

}