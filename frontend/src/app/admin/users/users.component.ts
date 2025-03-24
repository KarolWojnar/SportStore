import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, OnDestroy } from '@angular/core';
import { AdminService } from '../../service/admin.service';
import { UserDetails } from '../../model/user-dto';
import { NgClass, NgForOf, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-users',
  imports: [
    NgClass,
    RouterLink,
    NgIf,
    NgForOf
  ],
  standalone: true,
  templateUrl: './users.component.html',
  styleUrl: './users.component.scss'
})
export class UsersComponent implements OnInit, AfterViewInit, OnDestroy {
  users: UserDetails[] = [];
  isLoading: boolean = true;
  errorMessage: string = '';
  page: number = 0;
  hasMoreUsers: boolean = true;
  isLoadingNextData: boolean = false;
  private observer: IntersectionObserver | null = null;

  @ViewChild('lastUserElement', { static: false }) lastUserElement!: ElementRef;

  constructor(private adminService: AdminService) { }

  ngOnInit(): void {
    this.isLoading = true;
    this.loadUsers();
  }

  ngOnDestroy(): void {
    if (this.observer) {
      this.observer.disconnect();
    }
  }

  ngAfterViewInit(): void {
    this.setupIntersectionObserver();
  }

  loadUsers(): void {
    if (this.isLoadingNextData) return;

    this.isLoadingNextData = true;
    this.adminService.getAllUsers(this.page).subscribe({
      next: (response) => {
        this.users = [...this.users, ...response.users];
        this.hasMoreUsers = response.users.length > 9;
        this.isLoading = false;
        this.isLoadingNextData = false;
        setTimeout(() => this.updateObserver(), 100);
      },
      error: (error) => {
        this.isLoading = false;
        this.isLoadingNextData = false;
        if (this.page == 0) {
          console.error('Error loading users:', error);
          this.errorMessage = 'Failed to load users. Please try again later.';
        }
      }
    });
  }

  getRoleClass(role: string): string {
    return role === 'ROLE_ADMIN' ? 'text-bg-danger' : 'text-bg-success';
  }

  setupIntersectionObserver(): void {
    const options = {
      root: null,
      rootMargin: '100px',
      threshold: 0.5
    };

    this.observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting && this.hasMoreUsers && !this.isLoadingNextData) {
          console.log('Loading more users, page:', this.page + 1);
          this.page++;
          this.loadUsers();
        }
      });
    }, options);

    setTimeout(() => this.updateObserver(), 100);
  }

  updateObserver(): void {
    if (this.observer) {
      this.observer.disconnect();
    }

    if (this.lastUserElement && this.observer && this.hasMoreUsers) {
      console.log('Observer updated for new last element');
      this.observer.observe(this.lastUserElement.nativeElement);
    }
  }
}
