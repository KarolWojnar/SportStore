package org.shop.sportwebstore.model.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "customers")
public class Customer {
    private String id;
    @NotNull(message = "User is required.")
    private String userId;
    private String firstName;
    private String lastName;
    @NotNull(message = "Shipping address is required.")
    private String shippingAddress;
}
