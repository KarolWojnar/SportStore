package org.shop.sportwebstore.model.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.shop.sportwebstore.model.ProductInOrder;
import org.shop.sportwebstore.model.entity.Customer;
import org.shop.sportwebstore.model.entity.Order;
import org.shop.sportwebstore.model.entity.Product;
import org.shop.sportwebstore.model.entity.User;
import com.stripe.param.checkout.SessionCreateParams;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(source = "order.id", target = "id")
    @Mapping(source = "customer.firstName", target = "firstName")
    @Mapping(source = "customer.lastName", target = "lastName")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "order.status", target = "status")
    @Mapping(target = "paymentMethod", expression = "java(convertPaymentMethod(order.getPaymentMethod()))")
    @Mapping(source = "order.orderAddress", target = "shippingAddress")
    @Mapping(source = "order.totalPrice", target = "totalPrice")
    @Mapping(source = "order.deliveryDate", target = "deliveryDate")
    @Mapping(source = "order.orderDate", target = "orderDate")
    @Mapping(target = "productsDto", expression = "java(OrderProductDto.mapToDto(productsInOrder, products))")
    OrderDto mapToOrderDto(Order order, Customer customer, User user,
                           List<Product> products, List<ProductInOrder> productsInOrder);

    default SessionCreateParams.PaymentMethodType convertPaymentMethod(String paymentMethod) {
        return SessionCreateParams.PaymentMethodType.valueOf(paymentMethod);
    }
}
