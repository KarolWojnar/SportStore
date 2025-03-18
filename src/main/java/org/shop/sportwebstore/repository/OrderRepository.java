package org.shop.sportwebstore.repository;

import org.shop.sportwebstore.model.OrderStatus;
import org.shop.sportwebstore.model.entity.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<Order, String> {

    List<Order> findAllByStatusIsNotAndLastModifiedBefore(OrderStatus status, Date lastModified);
    Optional<Order> findBySessionId(String sessionId);
    List<Order> findAllByUserId(String userId);
    Optional<Order> findByIdAndUserId(String id, String id1);
}
