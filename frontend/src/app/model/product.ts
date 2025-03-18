export interface Product {
    id: string;
    name: string;
    price: number;
    description: string;
    quantity: number;
    image: string;
    rating: number;
    soldItems: number;
    categories: string[];
}

export interface ProductCart {
  id: string;
  name: string;
  image: string;
  price: number;
  quantity: number;
  totalQuantity: number;
}

export interface OrderBaseInfo {
  id: string;
  orderDate: Date;
  deliveryDate: Date;
  totalPrice: number;
  status: string;
}
