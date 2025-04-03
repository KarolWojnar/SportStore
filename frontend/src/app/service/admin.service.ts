import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserDetails, UserRole, UserStatus } from '../model/user-dto';
import { CategoryNew, ProductInfo, ProductsResponse } from '../model/product';
import { OrderBaseInfo } from '../model/order';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = 'http://localhost:8080/api/admin';

  constructor(private httpClient: HttpClient) { }

  getAllUsers(
    page: number,
    search: string = '',
    role: "ROLE_ADMIN" | "ROLE_CUSTOMER" | null = null,
    enabled: boolean | null = null
  ): Observable<{users: UserDetails[]}> {
    let params = new HttpParams()
      .set('page', page.toString());

    if (search) params = params.set('search', search);
    if (role) params = params.set('role', role);
    if (enabled !== null) params = params.set('enabled', enabled.toString());

    return this.httpClient.get<{users: UserDetails[]}>(`${this.apiUrl}/users`, { params });
  }

  setActivationUser(userId: string, status: UserStatus) {
    return this.httpClient.patch(`${this.apiUrl}/users/${userId}`, status);
  }

  setRole(id: string, role: UserRole) {
    return this.httpClient.patch(`${this.apiUrl}/users/${id}/role`, role);
  }

  getAllProducts(page: number, search?: string, category: string[] = []): Observable<ProductsResponse> {
    let params = new HttpParams().set('page', page.toString());

    if (search) {
      params = params.set('search', search);
    }

    if (category.length > 0) {
      params = params.set('categories', category.join(', '));
    }

    return this.httpClient.get<ProductsResponse>(`${this.apiUrl}/products`, { params });
  }

  updateProduct(productId: string, editedProduct: ProductInfo): Observable<ProductInfo> {
    return this.httpClient.patch<ProductInfo>(`${this.apiUrl}/products/${productId}`, editedProduct);
  }

  changeAvailability(productId: string, newAvailability: boolean) {
    return this.httpClient.patch(`${this.apiUrl}/products/${productId}/available`, newAvailability);
  }

  addProduct(formData: FormData) {
    return this.httpClient.post(`${this.apiUrl}/products`, formData);
  }

  addCategory(category: { name: string }): Observable<CategoryNew> {
    return this.httpClient.post<CategoryNew>(`${this.apiUrl}/categories`, category);
  }

  getOrders(page: number = 0, selectedStatus: string | null): Observable<OrderBaseInfo[]>{
    let params = new HttpParams().set('page', page);

    if (selectedStatus) {
      params = params.set('status', selectedStatus);
    }
    return this.httpClient.get<OrderBaseInfo[]>(`${this.apiUrl}/orders`, { params });
  }

  cancelOrder(orderId: string) {
    return this.httpClient.patch(`${this.apiUrl}/orders/${orderId}`, null);
  }
}
