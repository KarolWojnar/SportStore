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
import { ThemeService } from '../service/theme.service';
import { AuthService } from '../service/auth.service';

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

  constructor(private themeService: ThemeService,
              private authService: AuthService,
              private router: Router) { }

  ngOnInit(): void {
    this.authService.isLoggedIn().subscribe((response) => {
      this.isLoggedIn = response.isLoggedIn;
      if (this.isLoggedIn) {
        this.themeService.loadThemePreference().subscribe((isDarkMode) => {
          this.isDarkMode = isDarkMode.isDarkMode;
          this.themeService.applyTheme(this.isDarkMode);
        });
        this.authService.getRole().subscribe((isAdmin) => {
          this.isAdmin = isAdmin.role === 'ROLE_ADMIN';
        });
      }
    });
  }

  toggleTheme() {
    this.isDarkMode = !this.isDarkMode;
    this.themeService.toggleTheme(this.isDarkMode);
  }

  logout() {
    this.authService.logout().subscribe( {
      next: () => {
        this.isLoggedIn = false;
        this.isAdmin = false;
        this.router.navigate(['/login']).then(() => {
          window.location.reload();
        });
      },
      error: (err) => {
        console.error('Logout failed:', err);
      }
    });
  }
}
