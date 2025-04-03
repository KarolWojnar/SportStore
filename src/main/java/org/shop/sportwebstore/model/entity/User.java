package org.shop.sportwebstore.model.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;
import org.shop.sportwebstore.model.Roles;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Getter
@Setter
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
    @Builder.Default
    private boolean enabled = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return email.equals(user.email) && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, id);
    }
}
