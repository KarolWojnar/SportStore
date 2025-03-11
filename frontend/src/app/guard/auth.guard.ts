import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../service/auth.service';
import { catchError, map, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate() {
    return this.authService.isLoggedIn().pipe(
      map((response) => {
        if (response.isLoggedIn) {
          return true;
        } else {
          this.router.navigate(['/']).then(() => {
            window.location.reload();
          });
          return false;
        }
      }),
      catchError(() => {
        this.router.navigate(['/']).then(() => {
          window.location.reload();
        });
        return of(false);
      })
    );
  }
}
