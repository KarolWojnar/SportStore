package org.shop.sportwebstore.exception;

public class ShopException extends RuntimeException {
    public ShopException(String message) {
        super(message);
    }
    public ShopException(String message, Throwable cause) {
        super(message, cause);
    }
}
