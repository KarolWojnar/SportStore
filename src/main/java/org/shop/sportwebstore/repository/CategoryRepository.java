package org.shop.sportwebstore.repository;

import org.shop.sportwebstore.model.entity.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, String> {
}
