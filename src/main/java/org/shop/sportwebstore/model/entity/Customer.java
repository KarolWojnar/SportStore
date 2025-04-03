package org.shop.sportwebstore.model.entity;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.shop.sportwebstore.model.ShippingAddress;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "customers")
public class Customer {
    @Id
    private String id;
    @NotNull(message = "User is required.")
    private String userId;
    private String firstName;
    private String lastName;
    @NotNull(message = "Shipping address is required.")
    private ShippingAddress shippingAddress;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Customer customer = (Customer) o;

        return userId.equals(customer.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
