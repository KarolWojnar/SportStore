package org.shop.sportwebstore.service.store;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shop.sportwebstore.exception.PaymentException;
import org.shop.sportwebstore.exception.UserException;
import org.shop.sportwebstore.model.OrderStatus;
import org.shop.sportwebstore.model.dto.OrderBaseDto;
import org.shop.sportwebstore.model.dto.OrderDto;
import org.shop.sportwebstore.model.dto.OrderProductDto;
import org.shop.sportwebstore.model.entity.*;
import org.shop.sportwebstore.repository.CustomerRepository;
import org.shop.sportwebstore.repository.OrderRepository;
import org.shop.sportwebstore.repository.ProductRepository;
import org.shop.sportwebstore.repository.UserRepository;
import org.shop.sportwebstore.service.user.EmailService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public String createOrder(Cart cart, Customer customer, double totalPrice) {
        Order order = orderRepository.save(new Order(
                cart.getProducts(),
                customer.getUserId(),
                customer.getShippingAddress(),
                Math.round(totalPrice) / 100.0
            )
        );
        return order.getId();
    }

    public void updateOrderSessionId(String orderId, String sessionId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new PaymentException("Order not found."));
        order.setSessionId(sessionId);
        orderRepository.save(order);
    }

    @Transactional
    public void updateOrderStatusBySessionId(String sessionId, OrderStatus status) {
        Order order = orderRepository.findBySessionId(sessionId).orElseThrow(() -> new PaymentException("Order not found."));
        order.setNewStatus(status);
        emailService.sendEmailWithOrderDetails(order);
        orderRepository.save(order);
    }

    public List<OrderBaseDto> getUserOrders() {
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new UserException("User not found."));
        return orderRepository.findAllByUserId(user.getId()).stream().map(OrderBaseDto::mapToDto).toList();
    }

    public OrderDto getOrderById(String id) {
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new UserException("User not found."));
        Order order = orderRepository.findByIdAndUserId(id, user.getId()).orElseThrow(() -> new PaymentException("Order not found."));
        Customer customer = customerRepository.findByUserId(user.getId()).orElseThrow(() -> new UserException("Customer not found."));
        List<String> productIds = order.getProducts().keySet().stream().toList();
        List<Product> products = productRepository.findAllById(productIds);
        Map<Product, Integer> productsMap = products.stream().collect(
                java.util.stream.Collectors.toMap(
                        product -> product,
                        product -> order.getProducts().get(product.getId())
                )
        );
        return OrderDto.builder()
                .id(order.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(user.getEmail())
                .status(order.getStatus())
                .shippingAddress(order.getOrderAddress())
                .totalPrice(order.getTotalPrice())
                .deliveryDate(order.getDeliveryDate())
                .orderDate(order.getOrderDate())
                .productsDto(OrderProductDto.mapToDto(productsMap))
                .build();
    }
}
