package org.shop.sportwebstore.service;

import java.time.Duration;

public final class ConstantStrings {
    public static final String ACTIVATION_EMAIL_SUBJECT = "Activate your account";
    public static final String RESET_PASSWORD_SUBJECT = "Reset your password";
    public static final String RATE_PRODUCT_TITLE = "Rate product";

    public static final Duration ORDER_EXPIRATION = Duration.ofMinutes(30);

    public static final String ACTIVATION_EMAIL_BODY = """
        <html>
        <body>
            <h2 style="color: #007bff;">Welcome to our store!</h2>
            <p>Click the button below to activate your account:</p>
            <p><a href='%s' style='display: inline-block; padding: 10px 20px; background-color: #28a745;
             color: white; text-decoration: none; border-radius: 5px;'>Activate Account</a></p>
            <p>Code will expire  %s</p>
            <p>If the button doesn't work, use the following link:</p>
            <p><a href='%s'>%s</a></p>
            <p>Best regards,<br>Sport Store</p>
        </body>
        </html>
        """;
    public static final String RESET_PASSWORD_BODY = """
        <html>
        <body>
            <h2 style="color: #007bff;">Reset your password</h2>
            <p>Click the button below to reset your password:</p>
            <p><a href='%s' style='display: inline-block; padding: 10px 20px; background-color: #28a745;
             color: white; text-decoration: none; border-radius: 5px;'>Reset Password</a></p>
             <p>Code will expire  %s</p>
            <p>If the button doesn't work, use the following link:</p>
            <p><a href='%s'>%s</a></p>
            <p>Best regards,<br>Sport Store</p>
        </body>
        </html>
        """;

}
