package org.shop.sportwebstore.repository;

import org.shop.sportwebstore.model.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndEnabled(String email, boolean enabled);
    void deleteAllByIdIn(Collection<String> id);
}
