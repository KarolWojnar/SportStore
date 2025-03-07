package org.shop.sportwebstore.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    public ErrorResponse(String message) {
        this.message = message;
    }



    private String message;
    private Map<String, String> details;

    public ErrorResponse(String message, Map<String, String> details) {
        this.message = message;
        this.details = details;
    }
}
