package org.shop.sportwebstore.model.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;
import org.shop.sportwebstore.model.Roles;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;
    @Size(min = 8, message = "Password must be have at least 8 characters.")
    private String password;
    @Email(message = "Email is not valid.")
    @NotEmpty(message = "Email is required.")
    @UniqueElements(message = "Email already exists.")
    @Indexed(unique = true)
    private String email;
    @Builder.Default
    private Roles role = Roles.ROLE_CUSTOMER;
    private boolean enabled = false;
}
