package org.shop.sportwebstore.repository;

import org.shop.sportwebstore.model.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
}
