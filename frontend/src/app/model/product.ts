

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
  productId: string;
  name: string;
  image: string;
  price: number;
  quantity: number;
  totalQuantity: number;
  rated: boolean;
}


