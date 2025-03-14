package org.shop.sportwebstore.model.dto;

import lombok.Data;

@Data
public class ResetPassword {
    private String password;
    private String confirmPassword;
    private String code;
}
