package org.shop.sportwebstore.service;

import org.shop.sportwebstore.model.ErrorResponse;
import org.shop.sportwebstore.model.dto.ProductDto;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import java.util.Map;
import java.util.stream.Collectors;

public class ValidationUtil {
    public static ErrorResponse buildValidationErrors(BindingResult bindingResult) {
        Map<String, String> details = bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage
                ));

        return new ErrorResponse("Validation failed", details);
    }

    public static void validProductData(ProductDto productDto) {
        if (productDto.getName() == null || productDto.getName().isEmpty()) {
            throw new IllegalArgumentException("Product name is required.");
        }
        if (productDto.getPrice() <= 0) {
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
