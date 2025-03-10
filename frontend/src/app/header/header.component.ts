import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { RouterLink } from '@angular/router';
import { faShoppingCart, faUser, faSignInAlt, faSignOutAlt, faSun, faMoon } from '@fortawesome/free-solid-svg-icons';
import { NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss',
  standalone: true,
  imports: [CommonModule, FontAwesomeModule, RouterLink, NgbDropdownModule]
})
export class HeaderComponent {

  isDarkMode = document.body.classList.contains('dark-mode') || false;

  faShoppingCart = faShoppingCart;
  faUser = faUser;
  faSignInAlt = faSignInAlt;
  faSignOutAlt = faSignOutAlt;
  faSun = faSun;
  faMoon = faMoon;

  isLoggedIn = false;

  toggleLogin() {
    this.isLoggedIn = !this.isLoggedIn;
  }

  toggleTheme() {
    this.isDarkMode = !this.isDarkMode;
    document.body.classList.toggle('dark-mode', this.isDarkMode);
  }
}
