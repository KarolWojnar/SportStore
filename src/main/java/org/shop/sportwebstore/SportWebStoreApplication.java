package org.shop.sportwebstore;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Objects;

@SpringBootApplication
@EnableScheduling
public class SportWebStoreApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        System.setProperty("JWT_SECRET", Objects.requireNonNull(dotenv.get("JWT_SECRET")));
        System.setProperty("FORUM_MAIL_PASSWORD", Objects.requireNonNull(dotenv.get("FORUM_MAIL_PASSWORD")));
        System.setProperty("FORUM_MAIL_USERNAME", Objects.requireNonNull(dotenv.get("FORUM_MAIL_USERNAME")));
        System.setProperty("STRIPE_SECRET", Objects.requireNonNull(dotenv.get("STRIPE_SECRET")));
        System.setProperty("WEBHOOK_KEY", Objects.requireNonNull(dotenv.get("WEBHOOK_KEY")));
        SpringApplication.run(SportWebStoreApplication.class, args);
    }

}
