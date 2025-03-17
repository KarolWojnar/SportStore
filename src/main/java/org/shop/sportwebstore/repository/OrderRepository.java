package org.shop.sportwebstore.repository;

import org.shop.sportwebstore.model.entity.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String>{
}
