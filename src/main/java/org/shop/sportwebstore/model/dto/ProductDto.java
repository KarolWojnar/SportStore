package org.shop.sportwebstore.model.dto;

import lombok.Data;
import org.shop.sportwebstore.model.entity.Category;
import org.shop.sportwebstore.model.entity.Product;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Data
public class ProductDto {
    private String name;
    private String description;
    private double price;
    private int quantity;
    private String image;
    private List<String> categories;

    public static ProductDto toDto(Product product) {
        ProductDto productDto = new ProductDto();
        productDto.setName(product.getName());
        productDto.setDescription(product.getDescription());
        productDto.setPrice(product.getPrice());
        productDto.setQuantity(product.getAmountLeft());
        productDto.setImage(product.getImageUrl());
        productDto.setCategories(product.getCategories().stream().map(Category::getName).collect(toList()));
        return productDto;
    }
}
