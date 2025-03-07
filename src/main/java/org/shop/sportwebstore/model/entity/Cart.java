package org.shop.sportwebstore.model.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Cart {
    private String id;
    private String userId;
    private Map<String, Integer> products;

    public Cart() {
        this.products = new HashMap<>();
    }

    public Cart(String userId) {
        this.userId = userId;
        this.products = new HashMap<>();
    }

    public void addProduct(String productId, int quantity) {
        products.put(productId, products.getOrDefault(productId, 0) + quantity);
    }
}
