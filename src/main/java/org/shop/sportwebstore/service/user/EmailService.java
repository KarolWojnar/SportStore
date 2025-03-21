package org.shop.sportwebstore.service.user;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.shop.sportwebstore.exception.UserException;
import org.shop.sportwebstore.model.ProductInOrder;
import org.shop.sportwebstore.model.entity.*;
import org.shop.sportwebstore.repository.CustomerRepository;
import org.shop.sportwebstore.repository.ProductRepository;
import org.shop.sportwebstore.repository.UserRepository;
import org.shop.sportwebstore.service.ConstantStrings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.shop.sportwebstore.service.ConstantStrings.ORDER_SUMMARY_EMAIL_BODY;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    @Value("${spring.mail.username}")
    private String sender;

    @Value("${front.url}")
    private String url;

    private final JavaMailSender javaMailSender;

    public void sendEmailActivation(String email, Activation activation) {
        try {
            String expiration = activation.getExpiresAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            String urlLink = url + "activate/" + activation.getActivationCode();
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(sender);
            helper.setTo(email);
            helper.setSubject(ConstantStrings.ACTIVATION_EMAIL_SUBJECT);
            helper.setText(ConstantStrings.ACTIVATION_EMAIL_BODY
                    .formatted(urlLink, expiration, urlLink, urlLink), true);
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send activation email", e);
        }
    }

    public void sendEmailResetPassword(String email, Activation activation) {
        try {
            String expiration = activation.getExpiresAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            String urlLink = url + "reset-password/" + activation.getActivationCode();
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(sender);
            helper.setTo(email);
            helper.setSubject(ConstantStrings.RESET_PASSWORD_SUBJECT);
            helper.setText(ConstantStrings.RESET_PASSWORD_BODY
                    .formatted(urlLink, expiration, urlLink, urlLink), true);
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send reset password email", e);
        }
    }

    public void sendEmailWithOrderDetails(Order order) {
        User user = userRepository.findById(order.getUserId()).orElseThrow(() -> new UserException("User not found."));
        Customer customer = customerRepository.findByUserId(user.getId()).orElseThrow(() -> new UserException("Customer not found."));
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(sender);
            helper.setTo(user.getEmail());
            helper.setSubject(ConstantStrings.ORDER_EMAIL_SUBJECT);
            String emailBody = String.format(ConstantStrings.ORDER_EMAIL_BODY,
                    customer.getFirstName(),
                    order.getId(),
                    order.getOrderDate(),
                    order.getTotalPrice(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    order.getOrderAddress().getAddress(),
                    order.getOrderAddress().getCity(),
                    order.getOrderAddress().getZipCode(),
                    order.getOrderAddress().getCountry(),
                    url + "profile",
                    url + "profile/orders/" + order.getId()
            );
            helper.setText(emailBody, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send order email", e);
        }
    }

    public void sendEmailToDelivered(Order order, User user, Customer customer) {
        StringBuilder itemsHtml = new StringBuilder();
        for (ProductInOrder item : order.getProducts()) {
            Optional<Product> product = productRepository.findById(item.getProductId());
            product.ifPresent(value -> itemsHtml.append(String.format("""
                    <tr>
                        <td style="padding: 10px; border-bottom: 1px solid #ddd;">%s</td>
                        <td style="padding: 10px; border-bottom: 1px solid #ddd;">%d</td>
                        <td style="padding: 10px; border-bottom: 1px solid #ddd;">%.2f€</td>
                    </tr>
                    """, value.getName(), item.getAmount(), item.getPrice())));
        }
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(sender);
            helper.setTo(user.getEmail());
            helper.setSubject(ConstantStrings.ORDER_SUMMARY_SUBJECT);
            String emailBody = getString(order, customer, itemsHtml);
            helper.setText(emailBody, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send delivered email", e);
        }
    }

    private @NotNull String getString(Order order, Customer customer, StringBuilder itemsHtml) {
        String urlOrder = url + "profile/orders/" + order.getId();
        String emailTemplate = ORDER_SUMMARY_EMAIL_BODY.replace("%", "%%");

        emailTemplate = emailTemplate
                .replace("%%s", "%s")
                .replace("%%n", "%n");
        return String.format(emailTemplate,
                customer.getFirstName() + " " + customer.getLastName(),
                order.getId(),
                order.getOrderDate(),
                order.getTotalPrice(),
                customer.getFirstName(),
                customer.getLastName(),
                order.getOrderAddress().getAddress(),
                order.getOrderAddress().getCity(),
                order.getOrderAddress().getZipCode(),
                order.getOrderAddress().getCountry(),
                itemsHtml.toString(),
                urlOrder);
    }
}
