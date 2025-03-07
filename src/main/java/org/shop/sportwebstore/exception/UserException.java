package org.shop.sportwebstore.exception;

public class UserException extends ShopException {
    public UserException(String message) {
        super(message);
    }
    public UserException(String message, Throwable cause) {
        super(message, cause);
    }
}
