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
import lombok.extern.slf4j.Slf4j;
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
import org.shop.sportwebstore.service.ConstantStrings;
import org.shop.sportwebstore.service.user.SecurityContextWrapper;
import org.shop.sportwebstore.service.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final CartService cartService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderService orderService;
    private final SecurityContextWrapper securityContextWrapper;
    private final UserService userService;
    private final ObjectMapper objectMapper;

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
        BigDecimal shippingPrice = orderDto.getDeliveryTime().equals(DeliveryTime.STANDARD)
                ? ConstantStrings.STANDARD_SHIPPING
                : ConstantStrings.EXPRESS_SHIPPING;

        BigDecimal cartTotal = cartService.calculateTotalPrice(cart);
        BigDecimal totalPrice = cartTotal.add(shippingPrice)
                .multiply(new BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP);

        String orderId = orderService.createOrder(cart, customer, totalPrice, orderDto.getPaymentMethod());
        String url = preparePaymentTemplate(orderDto, totalPrice.longValueExact(), orderId);
        log.info("Start payment for {}", customer.getUserId());
        cartService.deleteCart(customer.getUserId());
        return url;
    }

    @Transactional
    public String createRepayment(String orderId) {
        OrderDto orderDto = orderService.getOrderById(orderId);
        BigDecimal totalPriceInCents = orderDto.getTotalPrice()
                .multiply(new BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP);
        return preparePaymentTemplate(orderDto, totalPriceInCents.longValueExact(), orderId);
    }

    private String preparePaymentTemplate(OrderDto orderDto, long totalPrice, String orderId) {
        Stripe.apiKey = stripeSecretKey;
        try {
            var productData = com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName("SportWebStore")
                            .build();

            var priceData = com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("eur")
                            .setUnitAmount(totalPrice)
                            .setProductData(productData)
                            .build();

            var items = com.stripe.param.checkout.SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(priceData)
                            .build();

            com.stripe.param.checkout.SessionCreateParams sessionCreateParams =
                    com.stripe.param.checkout.SessionCreateParams.builder()
                            .setMode(com.stripe.param.checkout.SessionCreateParams.Mode.PAYMENT)
                            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.valueOf(orderDto.getPaymentMethod().name()))
                            .setCustomerEmail(orderDto.getEmail())
                            .setSuccessUrl(frontUrl + "order?paid=true&orderId=" + orderId)
                            .setCancelUrl(frontUrl + "order?paid=false&orderId=" + orderId)
                            .addLineItem(items)
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
        User user = securityContextWrapper.getCurrentUser()
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
        List<Product> products = cartService.checkCartProducts(cart);
        cartService.blockAmountItem(cart, products);

        BigDecimal totalPrice = cartService.calculateTotalPrice(cart);
        return OrderDto.builder()
                .firstName(name)
                .lastName(lastName)
                .shippingAddress(address)
                .totalPrice(totalPrice)
                .deliveryTime(DeliveryTime.STANDARD)
                .build();
    }

    public void cancelPayment() {
        User user = securityContextWrapper.getCurrentUser()
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

                String sessionId = objectMapper.registerModule(new ParameterNamesModule())
                        .registerModule(new JavaTimeModule())
                        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                        .readTree(rawJson).get("id").asText();

                orderService.updateOrderStatusBySessionId(sessionId, OrderStatus.PROCESSING);
            }
        } catch (Exception e) {
            throw new UserException("Error during payment." + e.getMessage());
        }
    }

}
