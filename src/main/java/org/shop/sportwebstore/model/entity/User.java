package org.shop.sportwebstore.model.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;
import org.shop.sportwebstore.model.Roles;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "users")
public class User {
    private String id;
    @Min(value = 8, message = "Password must be have at least 8 characters.")
    private String password;
    @Email(message = "Email is not valid.")
    @NotEmpty(message = "Email is required.")
    private String email;
    @Builder.Default
    private Roles role = Roles.ROLE_CUSTOMER;
}
