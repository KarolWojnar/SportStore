package org.shop.sportwebstore.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "products")
@Data
public class Product {

    @Id
    private String id;
    @Indexed
    private String name;
    @Indexed
    private double price;
    private int amountLeft;
    private String description;
    private String imageUrl;

    /**
     * integer - amount of user who rated
     * double - rating of product
     */
    private Map<Integer, Double> ratings;
    private int orders;
    private List<Category> categories;
}
