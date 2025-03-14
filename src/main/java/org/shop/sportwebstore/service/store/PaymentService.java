package org.shop.sportwebstore.service.store;

import com.stripe.Stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;
    private final CartRedisService cartRedisService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    @Value("${spring.stripe.secret}")
    private String stripeSecretKey;

    @Value("${front.url}")
    private String frontUrl;

    public String createPayment(OrderDto orderDto) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.PAYPAL)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontUrl + "payment/success")
                .setCancelUrl(frontUrl+ "payment/cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount((long) ((orderDto.getTotalPrice() + orderDto.getShippingPrice()) * 100L))
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Testowy produkt")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }

    public OrderDto getSummary() {
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("User not found."));
        Customer  customer = customerRepository.findByUserId(user.getId()).orElse(null);
        String name = null;
        String lastName = null;
        ShippingAddress address = null;
        if (customer != null) {
            name = customer.getFirstName();
            lastName = customer.getLastName();
            address = customer.getShippingAddress();
        }

        Cart cart = cartRedisService.getCart(user.getId());
        double totalPrice = calculateTotalPrice(cart);
        return OrderDto.builder()
                .firstName(name)
                .lastName(lastName)
                .email(user.getEmail())
                .shippingAddress(address)
                .totalPrice(totalPrice)
                .deliveryTime(DeliveryTime.STANDARD)
                .build();
    }

    private double calculateTotalPrice(Cart cart) {
        List<Product> products = productRepository.findAllById(cart.getProducts().keySet());
        double totalPrice = 0;
        for (Product product : products) {
            totalPrice += product.getPrice() * cart.getProducts().get(product.getId());
        }
        return totalPrice;
    }
}
