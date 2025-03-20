package org.shop.sportwebstore.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.shop.sportwebstore.model.ShippingAddress;
import org.shop.sportwebstore.model.entity.Customer;
import org.shop.sportwebstore.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailsDto extends UserInfoDto {
    private static final Logger log = LoggerFactory.getLogger(UserDetailsDto.class);
    private String firstName;
    private String lastName;
    private ShippingAddress shippingAddress;

    public static UserDetailsDto toDto(User user, Customer customer) {
        UserDetailsDto userDetailsDto = new UserDetailsDto();
        userDetailsDto.setId(user.getId());
        userDetailsDto.setEmail(user.getEmail());
        userDetailsDto.setRole(user.getRole());
        userDetailsDto.setEnabled(user.isEnabled());
        if (customer == null) {
            return userDetailsDto;
        }
        userDetailsDto.setFirstName(customer.getFirstName());
        userDetailsDto.setLastName(customer.getLastName());
        userDetailsDto.setShippingAddress(customer.getShippingAddress());
        return userDetailsDto;
    }
}
