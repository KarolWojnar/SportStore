package org.shop.sportwebstore.model.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shop.sportwebstore.model.OrderStatus;
import org.shop.sportwebstore.model.ShippingAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;

@Document(collection = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Order {

    private static final Logger log = LoggerFactory.getLogger(Order.class);
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
    private Date lastModified = Date.from(java.time.Instant.now());
    private Date deliveryDate;
    private String paymentMethod;
    private double totalPrice;
    private String sessionId;

    public Order(Map<String, Integer> products, String userId, ShippingAddress address, double price, String paymentMethod) {
        this.products = products;
        this.userId = userId;
        this.orderAddress = address;
        this.totalPrice = price;
        this.paymentMethod = paymentMethod;

    }

    public void setNewStatus(OrderStatus status) {
        this.status = status;
        this.lastModified = Date.from(java.time.Instant.now());
    }

    public void setNextStatus() {
        int random = (int) (Math.random() * 13.0) + 1;
        switch (this.status) {
            case PROCESSING -> this.setNewStatus(random == 13 ? OrderStatus.ANNULLED : OrderStatus.SHIPPING);
            case SHIPPING -> this.setNewStatus(random == 13 ? OrderStatus.ANNULLED : OrderStatus.DELIVERED);
            case DELIVERED -> {
                if (random == 13) {
                    this.setNewStatus(OrderStatus.REFUNDED);
                }
                this.setDeliveryDate(Date.from(java.time.Instant.now()));
            }
        }
    }
}
