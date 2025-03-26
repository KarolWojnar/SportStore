package org.shop.sportwebstore.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateProductDto {
    private String productId;
    private int rating;
    private String orderId;
}
