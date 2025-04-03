import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { Router, RouterLink } from '@angular/router';
import {
  faShoppingCart,
  faUser,
  faSignInAlt,
  faSignOutAlt,
  faSun,
  faMoon,
  faUnlock
} from '@fortawesome/free-solid-svg-icons';
import { NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';
import { AuthService } from '../service/auth.service';
import { AuthStateService } from '../service/auth-state.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss',
  standalone: true,
  imports: [CommonModule, FontAwesomeModule, RouterLink, NgbDropdownModule]
})
export class HeaderComponent implements OnInit {

  isDarkMode = false;

  faShoppingCart = faShoppingCart;
  faUser = faUser;
  faSignInAlt = faSignInAlt;
  faSignOutAlt = faSignOutAlt;
  faSun = faSun;
  faMoon = faMoon;
  faUnlock = faUnlock;

  isLoggedIn = false;
  isAdmin = false;
  cartHasItems = true;

  constructor(private authService: AuthService,
              private router: Router,
              private authState: AuthStateService) { }

  ngOnInit(): void {
    this.authState.isLoggedIn$.subscribe((isLoggedIn) => {
      this.isLoggedIn = isLoggedIn;
      if (this.isLoggedIn) {
        this.authService.getRole().subscribe((roleResponse) => {
          this.authState.setAdmin(roleResponse.role === 'ROLE_ADMIN');
        });
      } else {
        this.isAdmin = false;
      }
    });

    this.authState.cartHasItems$.subscribe((cartHasItems) => {
      this.cartHasItems = cartHasItems;
    });

    this.authState.isDarkMode$.subscribe((isDarkMode) => {
      this.isDarkMode = isDarkMode;
      this.applyTheme(this.isDarkMode);
    });

    this.authState.isAdmin$.subscribe((isAdmin) => {
      this.isAdmin = isAdmin;
    });
  }

  applyTheme(isDarkMode: boolean) {
    if (isDarkMode) {
      document.body.classList.add('dark-mode');
    } else {
      document.body.classList.remove('dark-mode');
    }
  }

  toggleTheme() {
    this.isDarkMode = !this.isDarkMode;
    this.authState.setDarkMode(this.isDarkMode);
  }

  logout() {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: (err) => {
        console.error('Logout failed:', err);
      }
    });
  }
}
