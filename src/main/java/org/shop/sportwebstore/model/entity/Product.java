package org.shop.sportwebstore.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "products")
@Data
public class Product {

    @Id
    private String id;
    @Indexed
    private String name;
    private double price;
    private int amountLeft;
    private String description;
    private String imageUrl;
    private List<Category> categories;
}
