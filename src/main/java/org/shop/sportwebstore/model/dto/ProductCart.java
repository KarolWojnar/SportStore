package org.shop.sportwebstore.model.dto;

import lombok.Data;
import org.shop.sportwebstore.model.entity.Product;

@Data
public class ProductCart {
    private String id;
    private String name;
    private String image;
    private double price;
    private int quantity;
    private int totalQuantity;

    public static ProductCart toDto(Product product, int quantity) {
        ProductCart productCart = new ProductCart();
        productCart.setId(product.getId());
        productCart.setName(product.getName());
        productCart.setImage(product.getImageUrl());
        productCart.setPrice(product.getPrice());
        productCart.setQuantity(quantity);
        productCart.setTotalQuantity(product.getAmountLeft());
        return productCart;
    }

}
