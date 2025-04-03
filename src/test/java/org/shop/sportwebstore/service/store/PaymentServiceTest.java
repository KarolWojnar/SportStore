package org.shop.sportwebstore.service.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shop.sportwebstore.model.ShippingAddress;
import org.shop.sportwebstore.model.dto.OrderDto;
import org.shop.sportwebstore.model.entity.Cart;
import org.shop.sportwebstore.model.entity.Customer;
import org.shop.sportwebstore.model.entity.Product;
import org.shop.sportwebstore.model.entity.User;
import org.shop.sportwebstore.repository.CustomerRepository;
import org.shop.sportwebstore.repository.ProductRepository;
import org.shop.sportwebstore.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PaymentService paymentService;

    private final String userId = "user123";
    private final String productId = "prod123";
    private final String userEmail = "user@example.com";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userEmail);

    }

    @Test
    void getSummary_ShouldReturnOrderSummary() {
        User user = new User();
        user.setId(userId);
        user.setEmail(userEmail);

        Customer customer = new Customer();
        customer.setUserId(userId);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setShippingAddress(new ShippingAddress("Street", "City", "12345", "Country"));

        Cart cart = new Cart(userId);
        cart.addProduct(productId, 2);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(customerRepository.findByUserId(userId)).thenReturn(Optional.of(customer));
        when(cartService.getCart(userId)).thenReturn(cart);
        when(cartService.calculateTotalPrice(cart)).thenReturn(BigDecimal.valueOf(20.0));

        OrderDto result = paymentService.getSummary();

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals(BigDecimal.valueOf(20.0), result.getTotalPrice());
        verify(cartService).checkCartProducts(cart);
    }

    @Test
    void cancelPayment_ShouldCancelPaymentSuccessfully() {
        User user = new User();
        user.setId(userId);

        Cart cart = new Cart(userId);
        cart.addProduct(productId, 1);

        Product product = new Product();
        product.setId(productId);
        product.setAmountLeft(10);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(cartService.getCart(userId)).thenReturn(cart);
        when(productRepository.findAllById(any())).thenReturn(List.of(product));

        paymentService.cancelPayment();

        verify(cartService).cancelPayment(cart, List.of(product));
    }
}