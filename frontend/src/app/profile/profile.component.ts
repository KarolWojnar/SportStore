import { Component } from '@angular/core';
import { NgClass, NgIf } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import {
  faArrowLeft,
  faArrowRight,
  faBagShopping,
  faUser,
  faUserEdit
} from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-profile',
  imports: [
    NgIf,
    RouterLink,
    RouterOutlet,
    RouterLinkActive,
    FaIconComponent,
    NgClass
  ],
  standalone: true,
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent {
  protected readonly faBagShopping = faBagShopping;
  protected readonly faUser = faUser;
  protected readonly faUserEdit = faUserEdit;
  isSidebarCollapsed = false;

  toggleSidebar() {
    this.isSidebarCollapsed = !this.isSidebarCollapsed;
  }

  protected readonly faArrowRight = faArrowRight;
  protected readonly faArrowLeft = faArrowLeft;
}
