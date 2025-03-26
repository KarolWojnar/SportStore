package org.shop.sportwebstore.repository;

import org.shop.sportwebstore.model.entity.Category;
import org.shop.sportwebstore.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {

    @Query("{ 'name': { '$regex': ?0, '$options': 'i' }, 'categories.name': { '$in': ?1 }, available: {$in: [true, ?2]}, price: {$gte: ?3, $lte: ?4} }")
    Page<Product> findByNameMatchesRegexIgnoreCaseAndCategoriesIn(String name, List<String> categories, boolean available, int minPrice, int maxPrice, Pageable pageable);

    @Query("{ 'name': { '$regex': ?0, '$options': 'i' }, available: {$in: [true, ?1]}, price: {$gte: ?2, $lte: ?3} }")
    Page<Product> findByNameMatchesRegexIgnoreCase(String name, boolean available, int minPrice, int maxPrice, Pageable pageable);

    Product findTopByAvailableTrueAndAmountLeftGreaterThanOrderByPriceDesc(int amount);

    List<Product> findTop9ByAvailableTrueOrderByOrdersDesc();

    Optional<Product> findByIdAndAvailableTrue(String id);

    List<Product> findTop4ByCategoriesInAndIdNotAndAvailableTrue(Collection<List<Category>> categories, String id);
    Optional<Product> findByIdAndAmountLeftIsGreaterThanAndAvailableTrue(String id, int amountLeft);

    @Query("{ 'id': { '$eq': ?0 } }")
    @Update("{ $inc: { amountLeft: ?1 } }")
    void incrementAmountLeftById(String id, int amountLeft);

    @Query("{ 'id': { '$eq': ?0 } }")
    @Update("{ $inc: { orders: ?1 } }")
    void incrementSoldById(String productId, int amountItems);

}
