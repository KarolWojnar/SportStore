package org.shop.sportwebstore.model;

import lombok.Data;

@Data
public class ShippingAddress {
    private String address;
    private String city;
    private String country;
    private String zipCode;
}
