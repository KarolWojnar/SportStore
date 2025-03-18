package org.shop.sportwebstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shop.sportwebstore.model.ActivationType;
import org.shop.sportwebstore.model.OrderStatus;
import org.shop.sportwebstore.model.entity.Activation;
import org.shop.sportwebstore.model.entity.Cart;
import org.shop.sportwebstore.model.entity.Order;
import org.shop.sportwebstore.model.entity.Product;
import org.shop.sportwebstore.repository.*;
import org.shop.sportwebstore.service.store.CartService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class SchedulerService {

    private final UserRepository userRepository;
    private final ActivationRepository activationRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final OrderRepository orderRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void clearInactive() {
        List<Activation> codes = activationRepository.findAllByExpiresAtBefore(java.time.LocalDateTime.now());
        List<String> userIds = codes.stream()
                .filter(c -> c.getType() == ActivationType.REGISTRATION)
                .map(Activation::getUserId)
                .toList();
        activationRepository.deleteAll(codes);
        clearInactiveUsers(userIds);
        log.info("Deleted {} accounts.", userIds.size());
    }

    private void clearInactiveUsers(Collection<String> ids) {
        customerRepository.deleteAllByUserIdIn(ids);
        userRepository.deleteAllByIdIn(ids);
    }

    @Scheduled(cron = "0 * * * * *")
    public void clearExpiredOrderCart() {
        List<Cart> carts = cartService.findCartsByIsOrderProcessing(true, Date.from(Instant.now()));
        carts.forEach(cart -> {
            List<Product> products = productRepository.findAllById(cart.getProducts().keySet());
            cartService.cancelPayment(cart, products);
            cartService.deleteCart(cart.getUserId());
        });
        log.info("Deleted {} carts. date: {}", carts.size(), Date.from(Instant.now()));
    }

    @Scheduled(cron = "0 0 * * * *")
    public void changeOrderStatus() {
        Date minusTwoDays = new Date(System.currentTimeMillis() - ConstantStrings.ORDER_CHANGE.toMillis());
        List<Order> orders = orderRepository
                .findAllByStatusIsNotAndLastModifiedBefore(OrderStatus.CREATED, minusTwoDays);
        for (Order order : orders) {
            order.setNextStatus();
        }
        log.info("Changed {} orders status. date: {}", orders.size(),
                Date.from(Instant.now()));
        orderRepository.saveAll(orders);
    }
}
