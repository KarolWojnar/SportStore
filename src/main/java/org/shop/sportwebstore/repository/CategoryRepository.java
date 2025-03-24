package org.shop.sportwebstore.repository;

import org.shop.sportwebstore.model.entity.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CategoryRepository extends MongoRepository<Category, String> {
    List<Category> findAllByNameIn(List<String> names);
}
