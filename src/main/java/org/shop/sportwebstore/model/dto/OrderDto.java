package org.shop.sportwebstore.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.Builder;
import lombok.Data;
import org.shop.sportwebstore.model.DeliveryTime;
import org.shop.sportwebstore.model.OrderStatus;
import org.shop.sportwebstore.model.ShippingAddress;

import java.util.Date;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private List<OrderProductDto> productsDto;
    private OrderStatus status;
    private ShippingAddress shippingAddress;
    private double totalPrice;
    private Date deliveryDate;
    private Date orderDate;
    private DeliveryTime deliveryTime;
    private SessionCreateParams.PaymentMethodType paymentMethod;
}

