package org.shop.sportwebstore.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.Builder;
import lombok.Data;
import org.shop.sportwebstore.model.DeliveryTime;
import org.shop.sportwebstore.model.ShippingAddress;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private ShippingAddress shippingAddress;
    private double totalPrice;
    private DeliveryTime deliveryTime;
    private SessionCreateParams.PaymentMethodType paymentMethod;
}

