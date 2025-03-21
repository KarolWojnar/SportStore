package org.shop.sportwebstore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductInOrder {
    private String productId;
    private int amount;
    private double price;
    private boolean isRated;

    public ProductInOrder(String productId, int amount, double price) {
        this.productId = productId;
        this.amount = amount;
        this.price = price;
        this.isRated = false;
    }
}
