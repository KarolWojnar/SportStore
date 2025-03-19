export interface UserDto extends UserLoginDto{
  confirmPassword?: string;
  firstName?: string;
  lastName?: string;
  shippingAddress?: ShippingAddress | null;
  role?: string;
}

export interface UserLoginDto {
  email: string;
  password: string;
}

export interface CustomerDto {
  id?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  shippingAddress?: ShippingAddress;
  totalPrice?: number;
  deliveryTime?: string;
  paymentMethod?: string;
  shippingPrice?: number;
}

export interface ShippingAddress {
  address: string;
  city: string;
  zipCode: string;
  country: string;
}

export interface UserDetails {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  shippingAddress: ShippingAddress;
  role: string;
  enabled: boolean;
}
