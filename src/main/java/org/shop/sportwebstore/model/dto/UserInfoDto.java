package org.shop.sportwebstore.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.shop.sportwebstore.model.Roles;
import org.shop.sportwebstore.model.entity.User;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class UserInfoDto {
    private String id;
    private String email;
    private Roles role;
    private boolean enabled;

    public static UserInfoDto mapToDto(User user) {
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setEmail(user.getEmail());
        userInfoDto.setRole(user.getRole());
        userInfoDto.setId(user.getId());
        userInfoDto.setEnabled(user.isEnabled());
        return userInfoDto;
    }
}
