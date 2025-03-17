package org.shop.sportwebstore.model.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shop.sportwebstore.model.OrderStatus;
import org.shop.sportwebstore.model.ShippingAddress;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;

@Document(collection = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Order {

    @Id
    private String id;
    @NotNull(message = "User id is required.")
    private String userId;

    /**
     * key - product id
     * value - amount of product
     */
    @NotNull(message = "Products are required.")
    private Map<String, Integer> products;
    private OrderStatus status = OrderStatus.CREATED;
    private ShippingAddress orderAddress;
    private Date orderDate = Date.from(java.time.Instant.now());
    private Date lastModified;
    private Date deliveryDate;
    private double totalPrice;

    public Order(Map<String, Integer> products, String userId, ShippingAddress address, double price) {
        this.products = products;
        this.userId = userId;
        this.orderAddress = address;
        this.totalPrice = price;

    }

    public void setNewStatus(OrderStatus status) {
        this.status = status;
        this.lastModified = Date.from(java.time.Instant.now());
    }
}
