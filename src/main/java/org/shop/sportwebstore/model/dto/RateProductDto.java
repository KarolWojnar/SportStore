package org.shop.sportwebstore.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateProductDto {
    @NotNull(message = "Product ID is required")
    private String productId;
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5,  message = "Rating must be between 1 and 5")
    private int rating;
    @NotNull(message = "Order ID is required")
    private String orderId;
}
