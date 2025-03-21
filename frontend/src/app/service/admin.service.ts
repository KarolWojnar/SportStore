import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserDetails } from '../model/user-dto';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = 'http://localhost:8080/api/admin';

  constructor(private httpClient: HttpClient) { }

  getAllUsers(page: number): Observable<{users: UserDetails[]}> {
    return this.httpClient.get<{users: UserDetails[]}>(`${this.apiUrl}/users/${page}`);
  }
}
