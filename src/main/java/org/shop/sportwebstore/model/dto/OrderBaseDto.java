package org.shop.sportwebstore.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.shop.sportwebstore.model.entity.Order;

import java.util.Date;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderBaseDto {
    private String id;
    private Date orderDate;
    private Date deliveryDate;
    private String status;
    private double totalPrice;

    public static OrderBaseDto mapToDto(Order order) {
        return OrderBaseDto.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate() == null ? null : order.getOrderDate())
                .deliveryDate(order.getDeliveryDate() == null ? null : order.getDeliveryDate())
                .status(order.getStatus().toString())
                .totalPrice(order.getTotalPrice())
                .build();
    }
}
