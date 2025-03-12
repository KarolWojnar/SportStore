package org.shop.sportwebstore.repository;

import org.shop.sportwebstore.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {

    @Query("{ 'name': { '$regex': ?0, '$options': 'i' }, 'categories.name': { '$in': ?1 } }")
    Page<Product> findByNameMatchesRegexIgnoreCaseAndCategoriesIn(String name, List<String> categories, Pageable pageable);

    @Query("{ 'name': { '$regex': ?0, '$options': 'i' } }")
    Page<Product> findByNameMatchesRegexIgnoreCase(String name, Pageable pageable);


    List<Product> findTop9ByOrderByOrdersDesc();
}
