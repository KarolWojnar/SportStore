package org.shop.sportwebstore.service.store;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.exception.ProductException;
import org.shop.sportwebstore.exception.UserException;
import org.shop.sportwebstore.model.DeliveryTime;
import org.shop.sportwebstore.model.OrderStatus;
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
    private final OrderService orderService;
    private final UserService userService;

    @Value("${spring.stripe.secret}")
    private String stripeSecretKey;

    @Value("${spring.webhook.secret}")
    private String stripeWebhookSecret;

    @Value("${front.url}")
    private String frontUrl;

    @Transactional
    public String createPayment(OrderDto orderDto) {
        Customer customer = userService.findOrCreateCustomer(orderDto);
        Cart cart = cartService.getCart(customer.getUserId());
        double shippingPrice = orderDto.getDeliveryTime().equals(DeliveryTime.STANDARD) ? 0.0 : 10.0;
        double totalPrice = ((cartService.calculateTotalPrice(cart) + shippingPrice)) * 100;
        String orderId = orderService.createOrder(cart, customer, totalPrice, orderDto.getPaymentMethod());
        String url = preparePaymentTemplate(orderDto, (long) (totalPrice), orderId);
        log.info("Start payment for {}", customer.getUserId());
        cartService.deleteCart(customer.getUserId());
        return url;
    }

    @Transactional
    public String createRepayment(String orderId) {
        OrderDto orderDto = orderService.getOrderById(orderId);
        return preparePaymentTemplate(orderDto, (long) (orderDto.getTotalPrice() * 100), orderId);
    }

    private String preparePaymentTemplate(OrderDto orderDto, long totalPrice, String orderId) {
        Stripe.apiKey = stripeSecretKey;
        try {
            com.stripe.param.checkout.SessionCreateParams sessionCreateParams =
                    com.stripe.param.checkout.SessionCreateParams.builder()
                            .setMode(com.stripe.param.checkout.SessionCreateParams.Mode.PAYMENT)
                            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.valueOf(orderDto.getPaymentMethod().name()))
                            .setCustomerEmail(orderDto.getEmail())
                            .setSuccessUrl(frontUrl + "order?paid=true&orderId=" + orderId)
                            .setCancelUrl(frontUrl + "order?paid=false&orderId=" + orderId)
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

            Session session = com.stripe.model.checkout.Session.create(sessionCreateParams);
            orderService.updateOrderSessionId(orderId, session.getId());
            return session.getUrl();
        } catch (StripeException e) {
            throw new UserException("Error during payment." + e);
        }
    }

    @Transactional(rollbackFor = ProductException.class)
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

        double totalPrice = cartService.calculateTotalPrice(cart);
        return OrderDto.builder()
                .firstName(name)
                .lastName(lastName)
                .shippingAddress(address)
                .totalPrice(totalPrice)
                .deliveryTime(DeliveryTime.STANDARD)
                .build();
    }

    public void cancelPayment() {
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("User not found."));
        Cart cart = cartService.getCart(user.getId());
        List<Product> products = productRepository.findAllById(cart.getProducts().keySet());
        cartService.cancelPayment(cart, products);
    }

    public void webhook(String payload, String signature) {
        try {
            Event event = Webhook.constructEvent(payload, signature, stripeWebhookSecret);
            if (event.getType().equals("checkout.session.completed")) {

                String rawJson = event.getDataObjectDeserializer().getRawJson();

                ObjectMapper mapper = new ObjectMapper()
                        .registerModule(new ParameterNamesModule())
                        .registerModule(new JavaTimeModule())
                        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

                String sessionId = mapper.readTree(rawJson).get("id").asText();


                orderService.updateOrderStatusBySessionId(sessionId, OrderStatus.PROCESSING);
            }
        } catch (Exception e) {
            throw new UserException("Error during payment." + e.getMessage());
        }
    }

}
