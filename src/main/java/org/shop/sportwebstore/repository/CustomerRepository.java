package org.shop.sportwebstore.repository;

import org.shop.sportwebstore.model.entity.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends MongoRepository<Customer, String> {
    void deleteAllByUserIdIn(Collection<String> ids);
    Optional<Customer> findByUserId(String userId);
    List<Customer> findAllByUserIdIn(Collection<String> ids);
}
