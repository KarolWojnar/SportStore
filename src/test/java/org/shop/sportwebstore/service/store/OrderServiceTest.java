package org.shop.sportwebstore.service.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shop.sportwebstore.exception.PaymentException;
import org.shop.sportwebstore.exception.ProductException;
import org.shop.sportwebstore.model.OrderStatus;
import org.shop.sportwebstore.model.ProductInOrder;
import org.shop.sportwebstore.model.ShippingAddress;
import org.shop.sportwebstore.model.dto.OrderBaseDto;
import org.shop.sportwebstore.model.dto.OrderDto;
import org.shop.sportwebstore.model.entity.*;
import org.shop.sportwebstore.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderService orderService;

    private final String userId = "user123";
    private final String orderId = "order123";
    private final String productId = "prod123";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        String userEmail = "user@example.com";
        when(authentication.getName()).thenReturn(userEmail);

        User user = new User();
        user.setId(userId);
        user.setEmail(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
    }

    @Test
    void refundOrder_ShouldRefundOrder() {
        Order order = new Order();
        order.setStatus(OrderStatus.DELIVERED);
        order.setOrderDate(new Date());
        order.setProducts(new ArrayList<>());

        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(order));

        orderService.refundOrder(orderId);

        assertEquals(OrderStatus.REFUNDED, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void refundOrder_ShouldThrowWhenNotDelivered() {
        // Arrange
        Order order = new Order();
        order.setStatus(OrderStatus.SHIPPING);
        order.setProducts(new ArrayList<>());

        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(order));

        assertThrows(PaymentException.class, () -> orderService.refundOrder(orderId));
    }

    @Test
    void setOrderProductAsRated_ShouldMarkProductAsRated() {
        Order order = new Order();
        order.setProducts(new ArrayList<>(List.of(new ProductInOrder(productId, 1, BigDecimal.valueOf(10.0)))));

        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(order));

        orderService.setOrderProductAsRated(orderId, productId);

        assertTrue(order.getProducts().get(0).isRated());
        verify(orderRepository).save(order);
    }

    @Test
    void setOrderProductAsRated_ShouldThrowWhenAlreadyRated() {
        ProductInOrder productInOrder = new ProductInOrder(productId, 1, BigDecimal.valueOf(10.0));
        productInOrder.setRated(true);

        Order order = new Order();
        order.setProducts(new ArrayList<>(List.of(productInOrder)));

        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(order));

        assertThrows(ProductException.class, () ->
                orderService.setOrderProductAsRated(orderId, productId));
    }

    @Test
    void getOrderById_ShouldReturnOrderDetails() {
        Customer customer = new Customer();
        customer.setUserId(userId);
        when(customerRepository.findByUserId(userId)).thenReturn(Optional.of(customer));
        customer.setFirstName("John");
        customer.setLastName("Doe");

        Order order = new Order();
        order.setId(orderId);
        order.setProducts(new ArrayList<>(List.of(new ProductInOrder(productId, 1, BigDecimal.valueOf(10.0)))));
        order.setOrderAddress(new ShippingAddress("Street", "City", "12345", "Country"));
        order.setTotalPrice(new BigDecimal("10.0"));
        order.setPaymentMethod("CARD");

        Product product = new Product();
        product.setId(productId);
        product.setName("Test Product");

        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(order));
        when(customerRepository.findByUserId(userId)).thenReturn(Optional.of(customer));
        when(productRepository.findAllById(List.of(productId))).thenReturn(List.of(product));

        OrderDto result = orderService.getOrderById(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals(1, result.getProductsDto().size());
    }

    @Test
    void getUserOrders_ShouldReturnUserOrders() {
        Order order = new Order();
        order.setId(orderId);

        when(orderRepository.findAllByUserId(userId)).thenReturn(List.of(order));

        List<OrderBaseDto> result = orderService.getUserOrders();

        assertEquals(1, result.size());
        assertEquals(orderId, result.get(0).getId());
    }
}