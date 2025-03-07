package org.shop.sportwebstore.service;

import org.shop.sportwebstore.model.ErrorResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import java.util.Map;
import java.util.stream.Collectors;

public class ValidationUtil {
    public static ErrorResponse buildValidationErrors(BindingResult bindingResult) {
        Map<String, String> details = bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage
                ));

        return new ErrorResponse("Validation failed", details);
    }
}
