import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthStateService } from './auth-state.service';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {

  private apiUrl = 'http://localhost:8080/api/users/theme'

  constructor(private httpClient: HttpClient,
              private authState: AuthStateService) { }

  loadThemePreference(): Observable<{ isDarkMode: boolean }> {
    return this.httpClient.get<{ isDarkMode: boolean }>(`${this.apiUrl}`).pipe(
      tap((response) => {
        this.authState.setDarkMode(response.isDarkMode);
      })
    );
  }

  toggleTheme(isDarkMode: boolean): Observable<any> {
    return this.httpClient.post(`${this.apiUrl}`, isDarkMode);
  }

  applyTheme(isDarkMode: boolean) {
    if (isDarkMode) {
      document.body.classList.add('dark-mode');
    } else {
      document.body.classList.remove('dark-mode');
    }
  }
}
