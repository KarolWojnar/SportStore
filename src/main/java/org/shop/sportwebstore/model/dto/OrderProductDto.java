package org.shop.sportwebstore.model.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class OrderProductDto {
    private String productId;
    private int quantity;
    private BigDecimal price;
    private String name;
    private String image;
    private boolean isRated;
}
