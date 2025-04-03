package org.shop.sportwebstore.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginStatus {
    private boolean isLoggedIn;
    private String role;

    public LoginStatus(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    public LoginStatus(String role) {
        this.role = role;
    }
}
