package org.shop.sportwebstore.service;

import org.shop.sportwebstore.model.dto.ProductDto;
import java.math.BigDecimal;

public class ValidationUtil {

    public static void validProductData(ProductDto productDto) {
        if (productDto.getName() == null || productDto.getName().isEmpty()) {
            throw new IllegalArgumentException("Product name is required.");
        }
        if (productDto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be greater than 0.");
        }
        if (productDto.getQuantity() < 0) {
            throw new IllegalArgumentException("Product quantity must be non-negative.");
        }
    }

    public static void validRestProduct(ProductDto productDto) {
        if (productDto.getDescription() == null || productDto.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Product description is required.");
        }
        if (productDto.getCategories() == null || productDto.getCategories().isEmpty()) {
            throw new IllegalArgumentException("Product categories are required.");
        }
    }
}
