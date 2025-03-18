package org.shop.sportwebstore.model.dto;

import lombok.Builder;
import lombok.Data;
import org.shop.sportwebstore.model.entity.Product;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class OrderProductDto {
    private String productId;
    private int quantity;
    private double price;
    private String name;
    private String image;

    public static List<OrderProductDto> mapToDto(Map<Product, Integer> productsMap) {
        return productsMap.entrySet().stream().map(entry -> {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            return OrderProductDto.builder()
                    .productId(product.getId())
                    .quantity(quantity)
                    .price(product.getPrice())
                    .name(product.getName())
                    .image(product.getImageUrl())
                    .build();
        }).toList();
    }
}
