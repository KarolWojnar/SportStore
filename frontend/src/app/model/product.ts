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

export interface ProductDetails {
  id: string;
  name: string;
  available?: boolean;
  price: number;
  quantity: number;
  rating: number;
  image?: string;
  soldItems: number;
  categories: string[];
}

export interface ProductInfo {
  id: string;
  name: string;
  price: number;
  quantity: number;
}

export interface ProductsResponse {
  totalElements: number;
  categories: string[];
  products: ProductDetails[];
}

export interface NewProduct {
  name: string;
  price: number;
  description: string;
  quantity: number;
  categories: string[];
}

export interface CategoryNew {
  name: string;
}

