import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UserDto } from '../model/user-dto';
import { catchError, Observable, tap, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private httpClient: HttpClient) { }

  registerUser(user: UserDto) {
    return this.httpClient.post(`${this.apiUrl}/users`, user);
  }

  login(user: UserDto): Observable<any> {
    return this.httpClient.post(`${this.apiUrl}/auth/login`, user).pipe(
      tap((response: any) => {
        if (response && response.token) {
          localStorage.setItem('token', response.token);
        }
      })
    );
  }

  logout(): Observable<any> {
    return this.httpClient.post(`${this.apiUrl}/auth/logout`, {}).pipe(
      tap(() => {
        localStorage.removeItem('token');
      })
    );
  }


  activateAccount(activationCode: string) {
    return this.httpClient.get(`${this.apiUrl}/auth/activate/${activationCode}`);
  }

  isLoggedIn(): Observable<any> {
    return this.httpClient.get<any>(`${this.apiUrl}/auth/isLoggedIn`).pipe(
      catchError((error) => {
        if (error.status === 401) {
          localStorage.removeItem('token');
          return throwError(() => new Error('User is not logged in'));
        } else {
          localStorage.removeItem('token');
          throw new Error('User is not logged in');
        }
      })
    );
  }

  getRole(): Observable<any> {
    return this.httpClient.get<any>(`${this.apiUrl}/auth/role`);
  }
}
