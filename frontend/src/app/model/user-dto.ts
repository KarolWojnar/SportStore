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

export interface CustomerDto extends CustomerDetails{
  id?: string;
  email?: string;
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

export interface UserStatus {
  userStatus: boolean;
}

export interface UserLoginStatus {
  isLoggedIn: boolean;
  role: string;
}

export interface UserDetails extends CustomerDetails {
  id: string;
  email: string;
  role: string;
  enabled: boolean;
}

export enum UserRole {
  ROLE_CUSTOMER,
  ROLE_ADMIN
}


export interface CustomerDetails {
  firstName?: string;
  lastName?: string;
  shippingAddress?: ShippingAddress;
}
