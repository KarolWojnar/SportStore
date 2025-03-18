package org.shop.sportwebstore.service.user;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.exception.UserException;
import org.shop.sportwebstore.model.entity.Activation;
import org.shop.sportwebstore.model.entity.Customer;
import org.shop.sportwebstore.model.entity.Order;
import org.shop.sportwebstore.model.entity.User;
import org.shop.sportwebstore.repository.CustomerRepository;
import org.shop.sportwebstore.repository.UserRepository;
import org.shop.sportwebstore.service.ConstantStrings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
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
                    url + "order-info/" + order.getId()
            );
            helper.setText(emailBody, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send order email", e);
        }
    }
}
