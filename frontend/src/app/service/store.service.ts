import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../model/product';

@Injectable({
  providedIn: 'root'
})
export class StoreService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private httpClient: HttpClient) { }

  getProducts(page: number = 0, size: number = 10,
              sort: string = 'id', direction: string = 'asc',
              search: string = '', categories: string[] = [])
    : Observable<{ products: Product[]; totalElements: number }> {

    const params = { page, size, sort, direction, search, categories };
    return this.httpClient.get<{ products: Product[]; totalElements: number }>(`${this.apiUrl}/store`, {params});
  }

  getCategories() {
    return this.httpClient.get<string[]>(`${this.apiUrl}/store/categories`);
  }
}
