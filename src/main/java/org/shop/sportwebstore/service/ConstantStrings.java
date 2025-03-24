package org.shop.sportwebstore.service;

import java.time.Duration;

public final class ConstantStrings {
    public static final String ACTIVATION_EMAIL_SUBJECT = "Activate your account";
    public static final String RESET_PASSWORD_SUBJECT = "Reset your password";
    public static final String ORDER_SUMMARY_SUBJECT = "Your order delivered!";
    public static final String ORDER_EMAIL_SUBJECT = "Your order - Sport Store";

    public static final Duration ORDER_EXPIRATION = Duration.ofMinutes(30);
    public static final Duration TOKEN_ACCESS_EXPIRATION = Duration.ofMinutes(60);
    public static final Duration ORDER_CHANGE = Duration.ofDays(2);
    public static final Duration ORDER_DELETE = Duration.ofDays(1);

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

    public static String ORDER_SUMMARY_EMAIL_BODY = """
    <html>
    <body style="font-family: Arial, sans-serif; color: #333; line-height: 1.6;">
        <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
            <h2 style="color: #007bff; text-align: center;">Thank you for your order!</h2>
            <p>Dear %s</p>
            <p>We are pleased to confirm your order with the following details:</p>
    
            <div style="background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin-bottom: 20px;">
                <h3 style="color: #007bff; margin-bottom: 10px;">Order Summary</h3>
                <p><strong>Order ID:</strong> %s</p>
                <p><strong>Order Date:</strong> %s</p>
                <p><strong>Total Amount:</strong> %s€</p>
            </div>
    
            <div style="background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin-bottom: 20px;">
                <h3 style="color: #007bff; margin-bottom: 10px;">Billing Details</h3>
                <p><strong>Name:</strong> %s %s</p>
                <p><strong>Address:</strong> %s, %s, %s, %s</p>
            </div>
    
            <div style="background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin-bottom: 20px;">
                <h3 style="color: #007bff; margin-bottom: 10px;">Ordered Items</h3>
                <table style="width: 100%; border-collapse: collapse;">
                    <thead>
                        <tr style="background-color: #007bff; color: white;">
                            <th style="padding: 10px; text-align: left;">Product</th>
                            <th style="padding: 10px; text-align: left;">Quantity</th>
                            <th style="padding: 10px; text-align: left;">Price</th>
                        </tr>
                    </thead>
                    <tbody>
                        %s
                    </tbody>
                </table>
            </div>

            <p>If you have any questions, feel free to contact our support team at <a href="mailto:support@sportstore.com" style="color: #007bff; text-decoration: none;">support@sportstore.com</a>.</p>
    
            <p>Thank you for shopping with us!</p>
            <p>Best regards,<br><strong>Sport Store Team</strong></p>
   
            <div style="text-align: center; margin-top: 20px;">
                <a href="%s" style="display: inline-block; padding: 10px 20px; background-color: #28a745; color: white; text-decoration: none; border-radius: 5px;">View Your Order</a>
            </div>
        </div>
    </body>
    </html>
    """;

    public static final String ORDER_EMAIL_BODY = """
    <html>
    <body style="font-family: Arial, sans-serif; color: #333; line-height: 1.6;">
        <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
            <h2 style="color: #007bff; text-align: center;">Thank you for your order!</h2>
            <p>Dear %s,</p>
            <p>We are pleased to confirm your order with the following details:</p>

            <div style="background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin-bottom: 20px;">
                <h3 style="color: #007bff; margin-bottom: 10px;">Order Summary</h3>
                <p><strong>Order ID:</strong> %s</p>
                <p><strong>Order Date:</strong> %s</p>
                <p><strong>Total Amount:</strong> %s</p>
            </div>

            <div style="background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin-bottom: 20px;">
                <h3 style="color: #007bff; margin-bottom: 10px;">Billing Details</h3>
                <p><strong>Name:</strong> %s %s</p>
                <p><strong>Address:</strong> %s, %s, %s, %s</p>
            </div>

            <p>You can track your order status in your <a href="%s" style="color: #007bff; text-decoration: none;">profile</a>.</p>

            <p>If you have any questions, feel free to contact our support team at <a href="mailto:support@sportstore.com" style="color: #007bff; text-decoration: none;">support@sportstore.com</a>.</p>

            <p>Thank you for shopping with us!</p>
            <p>Best regards,<br><strong>Sport Store Team</strong></p>

            <div style="text-align: center; margin-top: 20px;">
                <a href="%s" style="display: inline-block; padding: 10px 20px; background-color: #28a745; color: white; text-decoration: none; border-radius: 5px;">View Your Order</a>
            </div>
        </div>
    </body>
    </html>
    """;
}
