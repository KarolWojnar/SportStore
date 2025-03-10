import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UserDto } from '../model/user-dto';
import { Observable, tap } from 'rxjs';

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


  activateAccount(activationCode: string) {
    return this.httpClient.get(`${this.apiUrl}/auth/activate/${activationCode}`);
  }
}
