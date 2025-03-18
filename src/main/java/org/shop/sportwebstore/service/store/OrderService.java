package org.shop.sportwebstore.service.store;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shop.sportwebstore.exception.PaymentException;
import org.shop.sportwebstore.exception.UserException;
import org.shop.sportwebstore.model.OrderStatus;
import org.shop.sportwebstore.model.dto.OrderBaseDto;
import org.shop.sportwebstore.model.entity.Cart;
import org.shop.sportwebstore.model.entity.Customer;
import org.shop.sportwebstore.model.entity.Order;
import org.shop.sportwebstore.model.entity.User;
import org.shop.sportwebstore.repository.OrderRepository;
import org.shop.sportwebstore.repository.UserRepository;
import org.shop.sportwebstore.service.user.EmailService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

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
}
