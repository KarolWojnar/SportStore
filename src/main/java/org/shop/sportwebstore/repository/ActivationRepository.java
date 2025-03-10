package org.shop.sportwebstore.repository;

import org.shop.sportwebstore.model.ActivationType;
import org.shop.sportwebstore.model.entity.Activation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ActivationRepository extends MongoRepository<Activation, String> {
    List<Activation> findAllByExpiresAtBefore(LocalDateTime expiresAt);
    Optional<Activation> findByActivationCodeAndType(String activationCode, ActivationType type);
}
