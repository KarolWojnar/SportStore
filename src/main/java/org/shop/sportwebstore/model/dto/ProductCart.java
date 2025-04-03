package org.shop.sportwebstore.model.dto;

import lombok.Data;
import org.shop.sportwebstore.model.entity.Product;

import java.math.BigDecimal;

@Data
public class ProductCart {
    private String productId;
    private String name;
    private String image;
    private BigDecimal price;
    private int quantity;
    private int totalQuantity;

    public static ProductCart toDto(Product product, int quantity) {
        ProductCart productCart = new ProductCart();
        productCart.setProductId(product.getId());
        productCart.setName(product.getName());
        productCart.setImage(product.getImageUrl());
        productCart.setPrice(product.getPrice());
        productCart.setQuantity(quantity);
        productCart.setTotalQuantity(product.getAmountLeft());
        return productCart;
    }

}
