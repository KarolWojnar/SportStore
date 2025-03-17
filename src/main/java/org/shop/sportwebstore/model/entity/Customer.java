package org.shop.sportwebstore.model.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shop.sportwebstore.model.ShippingAddress;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
