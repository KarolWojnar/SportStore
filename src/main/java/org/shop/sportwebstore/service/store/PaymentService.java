package org.shop.sportwebstore.service.store;

import com.stripe.exception.StripeException;
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
    private final CartService cartService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    @Value("${spring.stripe.secret}")
    private String stripeSecretKey;

    @Value("${front.url}")
    private String frontUrl;

    public String createPayment(OrderDto orderDto) throws StripeException {
        //TODO: handle payment
        return "todo";
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

        Cart cart = cartService.getCart(user.getId());

        cartService.checkCartProducts(cart, true);

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

    public void cancelPayment() {
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("User not found."));
        Cart cart = cartService.getCart(user.getId());
        List<Product> products = productRepository.findAllById(cart.getProducts().keySet());
        cartService.cancelPayment(cart, products);
    }
}
