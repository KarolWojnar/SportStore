package org.shop.sportwebstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class SportWebStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportWebStoreApplication.class, args);
    }

}
