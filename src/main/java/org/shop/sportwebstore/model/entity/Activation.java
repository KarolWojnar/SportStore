package org.shop.sportwebstore.model.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;
import org.shop.sportwebstore.model.ActivationType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "activations")
@Data
@NoArgsConstructor
public class Activation {
    @Id
    private String id;
    @UniqueElements
    @NotNull
    private String activationCode;
    private String userId;
    private ActivationType type;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public Activation(String userId, ActivationType type) {
        this.userId = userId;
        this.type = type;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = createdAt.plusDays(2);
        this.activationCode = String.valueOf(UUID.randomUUID());
    }
}
