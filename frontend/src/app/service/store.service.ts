import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthStateService } from './auth-state.service';
import { CustomerDto } from '../model/user-dto';
import { Product, ProductCart } from '../model/product';
import { Order, OrderBaseInfo, OrderRatingProduct } from '../model/order';

@Injectable({
  providedIn: 'root'
})
export class StoreService {
  private apiUrl = 'http://localhost:8080/api/store';
  private apiUrlPayment = 'http://localhost:8080/api/payment';
  private apiUrlOrder = 'http://localhost:8080/api/orders';

  constructor(private httpClient: HttpClient,
              private authState: AuthStateService) { }

  getProducts(page: number = 0, size: number = 10,
              sort: string = 'id', direction: string = 'asc',
              search: string = '', minPrice?: number,
              maxPrice?: number, categories: string[] = [])
    : Observable<{ products: Product[]; totalElements: number }> {

    const params: any = { page, size, sort, direction, search, categories };
    if (minPrice !== undefined) params.minPrice = minPrice;
    if (maxPrice !== undefined) params.maxPrice = maxPrice;
    return this.httpClient.get<{ products: Product[]; totalElements: number }>(`${this.apiUrl}`, {params});
  }

  getCategories() {
    return this.httpClient.get<{categories: string[]}>(`${this.apiUrl}/categories`);
  }

  getFeaturedProducts() {
    return this.httpClient.get<{ products: Product[]}>(`${this.apiUrl}/featured`);
  }

  getProductDetails(id: string) {
    return this.httpClient.get<{product: Product, relatedProducts: Product[]}>(`${this.apiUrl}/${id}`);
  }

  getCart() {
    return this.httpClient.get<{products: ProductCart[]}>(`${this.apiUrl}/cart`);
  }

  addToCart(id: string) {
    return this.httpClient.post(`${this.apiUrl}/cart/add`, id);
  }

  removeOneFromCart(id: string) {
    return this.httpClient.post(`${this.apiUrl}/cart/remove`, id);
  }

  removeProduct(id: string): Observable<any> {
    return this.httpClient.delete(`${this.apiUrl}/cart/${id}`);
  }

  clearCart(): Observable<any> {
    return this.httpClient.delete(`${this.apiUrl}/cart`);
  }

  checkout(): Observable<{order: CustomerDto}> {
    return this.httpClient.get<{order: CustomerDto}>(`${this.apiUrlPayment}/summary`);
  }

  getTotalPrice(): Observable<{totalPrice: number}> {
    return this.httpClient.get<{totalPrice: number}>(`${this.apiUrl}/cart/totalPrice`);
  }

  cancelPayment(): Observable<any> {
    return this.httpClient.delete(`${this.apiUrlPayment}/cancel`);
  }

  sendRequest(id: string, imgElement: HTMLImageElement) {
    this.addToCart(id).subscribe({
      next: () => {
        this.authState.setCartHasItems(true);
        localStorage.setItem('cartHasItems', 'true');
        this.animateCartIcon(imgElement);
      },
      error: (err) => {
        console.error('Error adding to cart:', err);
      }
    });
  }

  animateCartIcon(imgElement: HTMLImageElement) {
    const cartIcon = document.querySelector('.cart-icon') as HTMLElement;
    if (!cartIcon) return;
    const flyingImg = imgElement.cloneNode() as HTMLImageElement;
    const rect = imgElement.getBoundingClientRect();

    flyingImg.style.position = 'fixed';
    flyingImg.style.top = `${rect.top}px`;
    flyingImg.style.left = `${rect.left}px`;
    flyingImg.style.width = `${rect.width}px`;
    flyingImg.style.height = `${rect.height}px`;
    flyingImg.style.transition = 'all 0.8s ease-in-out';
    flyingImg.style.zIndex = '1000';
    flyingImg.style.opacity = '1';

    document.body.appendChild(flyingImg);

    const cartRect = cartIcon.getBoundingClientRect();
    setTimeout(() => {
      flyingImg.style.top = `${cartRect.top}px`;
      flyingImg.style.left = `${cartRect.left}px`;
      flyingImg.style.width = '30px';
      flyingImg.style.height = '30px';
      flyingImg.style.opacity = '0';
    }, 100);

    setTimeout(() => {
      document.body.removeChild(flyingImg);
    }, 900);

  }

  validCart() {
    return this.httpClient.get<{response: any}>(`${this.apiUrl}/cart/valid`);
  }

  goToPayment(customer: CustomerDto): Observable<{url: string}> {
    return this.httpClient.post<{url: string}>(`${this.apiUrlPayment}/create`, customer);
  }

  goToRepayment(orderId: string): Observable<{url: string}> {
    return this.httpClient.post<{url: string}>(`${this.apiUrlPayment}/repay`, orderId);
  }

  getUserOrders(): Observable<{orders: OrderBaseInfo[]}> {
    return this.httpClient.get<{orders: OrderBaseInfo[]}>(`${this.apiUrlOrder}`);
  }

  getOrderById(orderId: string) {
    return this.httpClient.get<{order: Order}>(`${this.apiUrlOrder}/${orderId}`);
  }

  rateProduct(rating: OrderRatingProduct) {
    return this.httpClient.patch(`${this.apiUrl}/rate`, rating);
  }

  cancelOrder(orderId: string) {
    return this.httpClient.patch(`${this.apiUrlOrder}/cancel/${orderId}`, null);
  }

  refundOrder(orderId: string) {
    return this.httpClient.patch(`${this.apiUrlOrder}/refund/${orderId}`, null);
  }

  getMaxPrice() {
    return this.httpClient.get<{maxPrice: number}>(`${this.apiUrl}/max-price`);
  }
}
