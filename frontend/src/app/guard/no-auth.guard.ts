import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../service/auth.service';
import { catchError, map, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class NoAuthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate() {
    return this.authService.isLoggedIn().pipe(
      map((response) => {
        if (response.isLoggedIn) {
          this.router.navigate(['/']);
          return false;
        } else {
          return true;
        }
      }),
      catchError(() => {
        this.router.navigate(['/']);
        return of(true);
      })
    );
  }
}
