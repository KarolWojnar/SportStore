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

    public String createOrder(Cart cart, Customer customer, double totalPrice, SessionCreateParams.PaymentMethodType paymentMethod) {
        List<ProductInOrder> productInOrder = cart.getProducts().entrySet().stream()
                .map(entry -> new ProductInOrder(
                        entry.getKey(),
                        entry.getValue(),
                        productRepository.findById(entry.getKey()).isPresent() ?
                                productRepository.findById(entry.getKey()).get().getPrice() : 0.0
                ))
                .collect(Collectors.toList());
        Order order = orderRepository.save(new Order(
                productInOrder,
                customer.getUserId(),
                customer.getShippingAddress(),
                Math.round(totalPrice) / 100.0,
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
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new UserException("User not found."));
        return orderRepository.findAllByUserId(user.getId()).stream().map(OrderBaseDto::mapToDto).toList();
    }

    public OrderDto getOrderById(String id) {
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new UserException("User not found."));
        Order order = orderRepository.findByIdAndUserId(id, user.getId()).orElseThrow(() -> new PaymentException("Order not found."));
        Customer customer = customerRepository.findByUserId(user.getId()).orElseThrow(() -> new UserException("Customer not found."));
        List<String> productIds = order.getProducts().stream().map(ProductInOrder::getProductId).toList();
        List<Product> products = productRepository.findAllById(productIds);
        List<ProductInOrder> productsInOrder = order.getProducts();
        return OrderDto.builder()
                .id(order.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(user.getEmail())
                .status(order.getStatus())
                .paymentMethod(SessionCreateParams.PaymentMethodType.valueOf(order.getPaymentMethod()))
                .shippingAddress(order.getOrderAddress())
                .totalPrice(order.getTotalPrice())
                .deliveryDate(order.getDeliveryDate())
                .orderDate(order.getOrderDate())
                .productsDto(OrderProductDto.mapToDto(productsInOrder, products))
                .build();
    }

    @Transactional
    public void handleNotPaidOrders(List<Order> orders) {
        for (Order order : orders) {
            order.getProducts().forEach(product -> productRepository.incrementAmountLeftById(product.getProductId(), product.getAmount()));
            orderRepository.delete(order);
        }
    }

    public void setOrderProductAsRated(String orderId, String productId) {
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new ProductException("User not found."));
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ProductException("Order not found."));
        order.getProducts().forEach(product -> {
            if (product.getProductId().equals(productId)) {
                if (product.isRated()) {
                    throw new ProductException("Product already rated.");
                }
                product.setRated(true);
            }
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
}
