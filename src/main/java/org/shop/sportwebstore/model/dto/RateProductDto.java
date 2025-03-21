package org.shop.sportwebstore.model.dto;

import lombok.Data;

@Data
public class RateProductDto {
    private String productId;
    private int rating;
    private String orderId;
}
