package org.shop.sportwebstore.repository;

import org.shop.sportwebstore.model.entity.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;


public interface CategoryRepository extends MongoRepository<Category, String> {
    @Query("{ name : { $in: ?0 } }")
    List<Category> findByNameIn(List<String> names);

    boolean existsByName(String name);
}
