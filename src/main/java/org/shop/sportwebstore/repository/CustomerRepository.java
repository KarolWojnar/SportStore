package org.shop.sportwebstore.repository;

import org.shop.sportwebstore.model.entity.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerRepository extends MongoRepository<Customer, String> {
}
