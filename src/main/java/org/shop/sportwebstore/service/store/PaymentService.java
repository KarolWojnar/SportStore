package org.shop.sportwebstore.service.store;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.model.DeliveryTime;
import org.shop.sportwebstore.model.ShippingAddress;
import org.shop.sportwebstore.model.dto.OrderDto;
import org.shop.sportwebstore.model.entity.Cart;
import org.shop.sportwebstore.model.entity.Customer;
import org.shop.sportwebstore.model.entity.Product;
import org.shop.sportwebstore.model.entity.User;
import org.shop.sportwebstore.repository.CustomerRepository;
import org.shop.sportwebstore.repository.ProductRepository;
import org.shop.sportwebstore.repository.UserRepository;
import org.shop.sportwebstore.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final UserRepository userRepository;
    private final CartService cartService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final UserService userService;

    @Value("${spring.stripe.secret}")
    private String stripeSecretKey;

    @Value("${front.url}")
    private String frontUrl;

    @Transactional
    public String createPayment(OrderDto orderDto) {
        Customer customer = userService.findOrCreateCustomer(orderDto);
        Cart cart = cartService.getCart(customer.getUserId());
        double shippingPrice = orderDto.getDeliveryTime().equals(DeliveryTime.STANDARD) ? 0 : 10;
        long totalPrice = (long) ((calculateTotalPrice(cart) + shippingPrice) * 100);
        return preparePaymentTemplate(orderDto, totalPrice);
    }

    private String preparePaymentTemplate(OrderDto orderDto, long totalPrice) {
        Stripe.apiKey = stripeSecretKey;
        try {
            com.stripe.param.checkout.SessionCreateParams sessionCreateParams =
                    com.stripe.param.checkout.SessionCreateParams.builder()
                            .setMode(com.stripe.param.checkout.SessionCreateParams.Mode.PAYMENT)
                            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.valueOf(orderDto.getPaymentMethod().name()))
                            .setCustomerEmail(orderDto.getEmail())
                            .setSuccessUrl(frontUrl + "/success")
                            .setCancelUrl(frontUrl + "/cancel")
                            .addLineItem(
                                    com.stripe.param.checkout.SessionCreateParams.LineItem.builder()
                                            .setQuantity(1L)
                                            .setPriceData(
                                                    com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData.builder()
                                                            .setCurrency("eur")
                                                            .setUnitAmount(totalPrice)
                                                            .setProductData(
                                                                    com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                            .setName("SportWebStore")
                                                                            .build()
                                                            )
                                                            .build()
                                            )
                                            .build()
                            )
                            .build();
            com.stripe.model.checkout.Session session = com.stripe.model.checkout.Session.create(sessionCreateParams);
            log.info("Session ID: {}", session.getId());
            return session.getUrl();
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    public OrderDto getSummary() {
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("User not found."));
        Customer customer = customerRepository.findByUserId(user.getId()).orElse(null);
        String name = null;
        String lastName = null;
        ShippingAddress address = null;
        if (customer != null) {
            name = customer.getFirstName();
            lastName = customer.getLastName();
            address = customer.getShippingAddress();
        }

        Cart cart = cartService.getCart(user.getId());

        cartService.checkCartProducts(cart, true);

        double totalPrice = calculateTotalPrice(cart);
        return OrderDto.builder()
                .firstName(name)
                .lastName(lastName)
                .shippingAddress(address)
                .totalPrice(totalPrice)
                .deliveryTime(DeliveryTime.STANDARD)
                .build();
    }

    public double calculateTotalPrice(Cart cart) {
        List<Product> products = productRepository.findAllById(cart.getProducts().keySet());
        double totalPrice = 0;
        for (Product product : products) {
            totalPrice += product.getPrice() * cart.getProducts().get(product.getId());
        }
        return totalPrice;
    }

    public void cancelPayment() {
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("User not found."));
        Cart cart = cartService.getCart(user.getId());
        List<Product> products = productRepository.findAllById(cart.getProducts().keySet());
        cartService.cancelPayment(cart, products);
    }
}
