package org.shop.sportwebstore.model.dto;


import lombok.Data;
import org.shop.sportwebstore.model.ShippingAddress;

@Data
public class CustomerDto {
    private String firstName;
    private String lastName;
    private ShippingAddress shippingAddress;
}