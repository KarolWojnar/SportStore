export interface UserDto extends UserLoginDto{
  confirmPassword?: string;
  firstName?: string;
  lastName?: string;
  shippingAddress?: string;
  role?: string;
}

export interface UserLoginDto {
  email: string;
  password: string;
}
