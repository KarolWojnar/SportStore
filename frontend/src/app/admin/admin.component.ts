import { Component } from '@angular/core';
import { FaIconComponent } from "@fortawesome/angular-fontawesome";
import { NgClass, NgIf } from "@angular/common";
import { RouterLink, RouterLinkActive, RouterOutlet } from "@angular/router";
import {
  faArrowLeft,
  faArrowRight,
  faBagShopping, faBoxesPacking,
  faUsers
} from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-admin',
  imports: [
    FaIconComponent,
    NgIf,
    RouterLinkActive,
    RouterOutlet,
    RouterLink,
    NgClass
  ],
  standalone: true,
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.scss'
})
export class AdminComponent {

  protected readonly faBagShopping = faBagShopping;
  protected readonly faArrowLeft = faArrowLeft;
  protected readonly faArrowRight = faArrowRight;
  protected readonly faBoxesPacking = faBoxesPacking;
  protected readonly faUsers = faUsers;
  isSidebarCollapsed = false;


  toggleSidebar() {
    this.isSidebarCollapsed = !this.isSidebarCollapsed;
  }

  isAdminHome(): boolean {
    return window.location.pathname === '/admin';
  }
}
