import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {

  private apiUrl = 'http://localhost:8080/api/users/theme'

  constructor(private httpClient: HttpClient) { }

  loadThemePreference(): Observable<any> {
    return this.httpClient.get<any>(`${this.apiUrl}`);
  }

  saveThemePreference(isDarkMode: boolean): Observable<void> {
    return this.httpClient.post<void>(`${this.apiUrl}`, isDarkMode);
  }

  toggleTheme(isDarkMode: boolean) {
    document.body.classList.toggle('dark-mode', isDarkMode);
    this.saveThemePreference(isDarkMode).subscribe();
  }

  applyTheme(isDarkMode: boolean): void {
    if (isDarkMode) {
      document.body.classList.add('dark-mode');
      document.body.classList.remove('light-mode');
    } else {
      document.body.classList.add('light-mode');
      document.body.classList.remove('dark-mode');
    }
  }
}
