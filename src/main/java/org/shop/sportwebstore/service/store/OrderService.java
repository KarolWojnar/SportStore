package org.shop.sportwebstore.service.store;

import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shop.sportwebstore.exception.PaymentException;
import org.shop.sportwebstore.exception.ProductException;
import org.shop.sportwebstore.exception.UserException;
import org.shop.sportwebstore.model.OrderStatus;
import org.shop.sportwebstore.model.ProductInOrder;
import org.shop.sportwebstore.model.dto.OrderBaseDto;
import org.shop.sportwebstore.model.dto.OrderDto;
import org.shop.sportwebstore.model.dto.OrderMapper;
import org.shop.sportwebstore.model.entity.*;
import org.shop.sportwebstore.repository.CustomerRepository;
import org.shop.sportwebstore.repository.OrderRepository;
import org.shop.sportwebstore.repository.ProductRepository;
import org.shop.sportwebstore.repository.UserRepository;
import org.shop.sportwebstore.service.user.EmailService;
import org.shop.sportwebstore.service.user.SecurityContextWrapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final SecurityContextWrapper securityContextWrapper;

    public String createOrder(Cart cart, Customer customer, BigDecimal totalPrice, SessionCreateParams.PaymentMethodType paymentMethod) {
        List<ProductInOrder> productInOrder = cart.getProducts().entrySet().stream()
                .map(entry -> {
                    Optional<Product> product = productRepository.findByIdAndAvailableTrue(entry.getKey());
                    BigDecimal price = product.map(Product::getPrice)
                            .orElse(BigDecimal.ZERO);
                        return new ProductInOrder(
                            entry.getKey(),
                            entry.getValue(),
                            price
                        );
                    }).collect(Collectors.toList());

        Order order = orderRepository.save(new Order(
                        productInOrder,
                        customer.getUserId(),
                        customer.getShippingAddress(),
                        totalPrice.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP),
                        paymentMethod.name()
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
        incrementSoldItems(order.getProducts());
        emailService.sendEmailWithOrderDetails(order);
        orderRepository.save(order);
    }

    private void incrementSoldItems(List<ProductInOrder> products) {
        products.forEach(product -> productRepository.incrementSoldById(product.getProductId(), product.getAmount()));
    }

    public List<OrderBaseDto> getUserOrders() {
        User user = securityContextWrapper.getCurrentUser()
                .orElseThrow(() -> new UserException("User not found."));
        return orderRepository.findAllByUserId(user.getId()).stream().map(OrderBaseDto::mapToDto).toList();
    }

    public OrderDto getOrderById(String id) {
        User user = securityContextWrapper.getCurrentUser()
                .orElseThrow(() -> new UserException("User not found."));
        Order order = orderRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new PaymentException("Order not found."));
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UserException("Customer not found."));
        List<String> productIds = order.getProducts().stream().map(ProductInOrder::getProductId).toList();
        List<Product> products = productRepository.findAllById(productIds);
        List<ProductInOrder> productsInOrder = order.getProducts();
        return orderMapper.mapToOrderDto(order, customer, user, products, productsInOrder);
    }

    @Transactional
    public void handleNotPaidOrders(List<Order> orders) {
        for (Order order : orders) {
            order.getProducts().forEach(product -> productRepository.incrementAmountLeftById(product.getProductId(), product.getAmount()));
            orderRepository.delete(order);
        }
    }

    public void setOrderProductAsRated(String orderId, String productId) {
        User user = securityContextWrapper.getCurrentUser()
                .orElseThrow(() -> new ProductException("User not found."));
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ProductException("Order not found."));
        order.getProducts().stream()
                .filter(product -> product.getProductId().equals(productId))
                .findFirst()
                .ifPresentOrElse(product -> {
                    if (product.isRated()) {
                        throw new ProductException("Product already rated.");
                    }
                    product.setRated(true);
                }, () -> {
                    throw new ProductException("Product not found in order.");
                });

        orderRepository.save(order);
    }

    public void sendOrderDeliveredEmail(Order order) {
        try {
            User user = userRepository.findById(order.getUserId()).orElseThrow(() -> new UserException("User not found."));
            Customer customer = customerRepository.findByUserId(user.getId()).orElseThrow(() -> new UserException("Customer not found."));
            emailService.sendEmailToDelivered(order, user, customer);
        } catch (Exception e) {
            log.error("Error sending email: {}", e.getMessage());
        }
    }

    public List<OrderBaseDto> getOrders(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size);
        if (status == null || status.isEmpty()) {
            return orderRepository.findAll(pageable).map(OrderBaseDto::mapToDto).toList();
        }
        return orderRepository.findAllByStatus(OrderStatus.valueOf(status), pageable).stream().map(OrderBaseDto::mapToDto).toList();
    }

    public void cancelOrder(String id, boolean isAdmin) {
        Order order;
        if (isAdmin) {
            order = orderRepository.findById(id).orElseThrow(() -> new ProductException("Order not found."));
        } else {
            order = findOrderByUserAndId(id);
        }
        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PROCESSING) {
            throw new PaymentException("Order already paid.");
        }
        order.getProducts().forEach(product -> productRepository.incrementAmountLeftById(product.getProductId(), product.getAmount()));
        order.setStatus(OrderStatus.ANNULLED);
        orderRepository.save(order);
    }

    public Order findOrderByUserAndId(String id) {
        User user = securityContextWrapper.getCurrentUser()
                .orElseThrow(() -> new UserException("User not found."));
        return orderRepository.findByIdAndUserId(id, user.getId()).orElseThrow(() -> new PaymentException("Order not found."));
    }

    public void refundOrder(String id) {
        Order order = findOrderByUserAndId(id);
        Instant fourteenDaysAgo = Instant.now().minus(14, ChronoUnit.DAYS);
        Date refundDeadline = Date.from(fourteenDaysAgo);

        if (order.getOrderDate().before(refundDeadline)) {
            throw new PaymentException("Order date is more than 14 days ago.");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new PaymentException("Order not delivered.");
        }
        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);
    }
}
