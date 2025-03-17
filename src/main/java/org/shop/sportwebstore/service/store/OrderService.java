package org.shop.sportwebstore.service.store;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shop.sportwebstore.model.entity.Cart;
import org.shop.sportwebstore.model.entity.Customer;
import org.shop.sportwebstore.model.entity.Order;
import org.shop.sportwebstore.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    public void createOrder(Cart cart, Customer customer, double totalPrice) {
        orderRepository.save(new Order(
                cart.getProducts(),
                customer.getUserId(),
                customer.getShippingAddress(),
                Math.round(totalPrice * 100.0) / 100.0
            )
        );
    }
}
