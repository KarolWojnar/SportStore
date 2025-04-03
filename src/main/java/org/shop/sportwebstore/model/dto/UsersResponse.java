package org.shop.sportwebstore.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UsersResponse {

    public UsersResponse(List<UserDetailsDto> users) {
        this.users = users;
    }

    private List<UserDetailsDto> users;


}
