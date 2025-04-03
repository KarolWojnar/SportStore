import { ShippingAddress } from './user-dto';
import { ProductCart } from './product';

export interface OrderBaseInfo {
  id: string;
  orderDate: Date;
  deliveryDate: Date;
  totalPrice: number;
  status: string;
}

export interface PaymentLink {
  url: string;
}

export interface Order extends OrderBaseInfo {
  shippingAddress: ShippingAddress;
  productsDto: ProductCart[];
  id: string;
  totalPrice: number;
  email: string;
  orderDate: Date;
  deliveryDate: Date;
  status: string;
  firstName: string;
  lastName: string;
}

export interface OrderRatingProduct {
  productId: string;
  rating: number;
  orderId: string;
}
