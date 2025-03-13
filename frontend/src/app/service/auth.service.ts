import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UserDto } from '../model/user-dto';
import { catchError, Observable, of, tap } from 'rxjs';
import { AuthStateService } from './auth-state.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private httpClient: HttpClient,
              private authState: AuthStateService
              ) { }

  registerUser(user: UserDto) {
    return this.httpClient.post(`${this.apiUrl}/users`, user);
  }

  login(user: UserDto): Observable<any> {
    return this.httpClient.post(`${this.apiUrl}/auth/login`, user).pipe(
      tap((response: any) => {
        if (response && response.token) {
          this.authState.setLoggedIn(true);
          localStorage.setItem('token', response.token);
          localStorage.setItem('isAdmin', response.role === 'ROLE_ADMIN' ? 'true' : 'false');
          localStorage.setItem('isDarkMode', response.isDarkMode ? 'true' : 'false');
          localStorage.setItem('cartHasItems', response.cartHasItems ? 'true' : 'false');
          this.authState.setCartHasItems(response.cartHasItems);
        }
      })
    );
  }

  logout(): Observable<any> {
    return this.httpClient.post(`${this.apiUrl}/auth/logout`, {}).pipe(
      tap(() => {
        this.authState.setLoggedIn(false);
        localStorage.removeItem('token');
        localStorage.removeItem('isAdmin');
        localStorage.removeItem('cartHasItems');
      })
    );
  }


  activateAccount(activationCode: string) {
    return this.httpClient.get(`${this.apiUrl}/auth/activate/${activationCode}`);
  }

  isLoggedIn(): Observable<{ isLoggedIn: boolean }> {
    return this.httpClient.get<{ isLoggedIn: boolean }>(`${this.apiUrl}/auth/isLoggedIn`).pipe(
      catchError(() => {
        this.authState.setLoggedIn(false);
        this.authState.setAdmin(false);
        localStorage.removeItem('token');
        localStorage.removeItem('isAdmin');
        return of({ isLoggedIn: false });
      })
    );
  }

  getRole(): Observable<any> {
    return this.httpClient.get<any>(`${this.apiUrl}/auth/role`).pipe(
      tap((response: any) => {
        if (response && response.role) {
          this.authState.setAdmin(response.role === 'ROLE_ADMIN');
        }
      })
    );
  }
}
