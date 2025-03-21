package org.shop.sportwebstore.model.dto;

import lombok.Builder;
import lombok.Data;
import org.shop.sportwebstore.exception.ProductException;
import org.shop.sportwebstore.model.ProductInOrder;
import org.shop.sportwebstore.model.entity.Product;

import java.util.List;

@Data
@Builder
public class OrderProductDto {
    private String productId;
    private int quantity;
    private double price;
    private String name;
    private String image;
    private boolean isRated;

    public static List<OrderProductDto> mapToDto(List<ProductInOrder> products, List<Product> productList) {
        return products.stream().map(product -> {
            Product product1 = productList.stream()
                    .filter(p -> p.getId().equals(product.getProductId()))
                    .findFirst().orElseThrow(() -> new ProductException("Product not found"));
            return OrderProductDto.builder()
                    .productId(product.getProductId())
                    .quantity(product.getAmount())
                    .price(product.getPrice())
                    .name(product1.getName())
                    .image(product1.getImageUrl())
                    .isRated(product.isRated())
                    .build();
        }).toList();
    }
}
