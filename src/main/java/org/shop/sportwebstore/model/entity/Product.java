package org.shop.sportwebstore.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@Document(collection = "products")
public class Product {

    @Id
    private String id;
    @Indexed
    private String name;
    @Indexed
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal price;
    private int amountLeft;
    private String description;
    private String imageUrl;
    private boolean available = true;

    /**
     * integer - amount of user who rated
     * double - rating of product
     */
    private Map<Integer, Double> ratings;
    private int orders;
    private List<Category> categories;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id != null && id.equals(product.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : 0;
    }
}
