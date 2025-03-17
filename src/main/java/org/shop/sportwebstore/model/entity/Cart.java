package org.shop.sportwebstore.model.entity;

import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class Cart {
    private String id;
    private String userId;
    private Map<String, Integer> products;
    private boolean isOrderProcessing = false;
    private Date lastModified = Date.from(java.time.Instant.now());

    public void setOrderProcessing(boolean orderProcessing) {
        this.isOrderProcessing = orderProcessing;
        this.lastModified = Date.from(java.time.Instant.now());
    }

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

    public int getQuantity(String productId) {
        return products.getOrDefault(productId, 0);
    }

    public void removeProduct(String id) {
        int quantity = products.get(id);
        if (quantity == 1) {
            products.remove(id);
        } else {
            products.put(id, quantity - 1);
        }

    }
}
