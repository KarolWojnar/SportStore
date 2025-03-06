package org.shop.sportwebstore.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.shop.sportwebstore.model.Roles;
import org.shop.sportwebstore.model.entity.Customer;
import org.shop.sportwebstore.model.entity.User;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    @Email(message = "Email is not valid.")
    @NotEmpty(message = "Email is required.")
    private String email;
    @Size(min = 8, message = "Password must be have at least 8 characters.")
    private String password;
    @Size(min = 8, message = "Confirm password must be have at least 8 characters.")
    private String confirmPassword;
    private String firstName;
    private String lastName;
    private String shippingAddress;
    private Roles role;

    public static User toUserEntity(UserDto userDto) {
        return User.builder()
                .email(userDto.getEmail())
                .password(userDto.password)
                .build();
    }

    public static Customer toCustomerEntity(UserDto userDto, User user) {
        return Customer.builder()
                .user(user)
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .shippingAddress(userDto.getShippingAddress())
                .build();
    }

    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public static UserDto toCustomerDto(Customer customer) {
        return UserDto.builder()
                .email(customer.getUser().getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .shippingAddress(customer.getShippingAddress())
                .role(customer.getUser().getRole())
                .build();
    }
}
