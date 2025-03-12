package org.shop.sportwebstore.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.shop.sportwebstore.model.entity.Category;
import org.shop.sportwebstore.model.entity.Product;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDto {
    private String name;
    private String description;
    private double price;
    private int quantity;
    private double rating;
    private String image;
    private int soldItems;
    private List<String> categories;

    public static ProductDto toDto(Product product) {
        ProductDto productDto = new ProductDto();
        productDto.setName(product.getName());
        productDto.setPrice(product.getPrice());
        productDto.setImage(product.getImageUrl());
        productDto.setRating(product.getRatings().values().iterator().next());
        productDto.setQuantity(product.getAmountLeft());
        productDto.setSoldItems(product.getOrders());
        productDto.setCategories(product.getCategories().stream().map(Category::getName).collect(toList()));
        return productDto;
    }
}
