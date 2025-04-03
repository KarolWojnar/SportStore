import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { AdminService } from '../../service/admin.service';
import { UserDetails, UserRole, UserStatus } from '../../model/user-dto';
import { NgClass, NgForOf, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged, Subject, takeUntil } from 'rxjs';
import { ConfirmationDialogComponent } from '../../shared/confirm-dialog/confirm-dialog.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-users',
  imports: [
    NgClass,
    RouterLink,
    NgIf,
    NgForOf,
    FormsModule
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
  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  @ViewChild('lastUserElement', { static: false }) lastUserElement!: ElementRef;
  search: string = '';
  role: "ROLE_ADMIN" | "ROLE_CUSTOMER" | null = null;
  enabled: boolean | null = null;
  private observer: IntersectionObserver | null = null;

  constructor(private adminService: AdminService,
              private dialog: MatDialog) { }

  ngOnInit(): void {
    this.setupSearchListener();
    this.loadUsers(true);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.disconnectObserver();
  }

  ngAfterViewInit(): void {
    this.setupIntersectionObserver();
  }

  private setupSearchListener(): void {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.resetAndLoadUsers();
    });
  }

  private resetAndLoadUsers(): void {
    this.page = 0;
    this.users = [];
    this.hasMoreUsers = true;
    this.loadUsers(true);
  }

  loadUsers(initialLoad: boolean = false): void {
    if (this.isLoadingNextData) return;

    initialLoad ? this.isLoading = true : this.isLoadingNextData = true;

    this.adminService.getAllUsers(this.page, this.search, this.role, this.enabled).subscribe({
      next: (response) => {
        this.users = [...this.users, ...response.users];
        this.hasMoreUsers = response.users.length === 10;
        this.errorMessage = '';
      },
      error: (error) => {
        if (error.status === 404) {
          this.hasMoreUsers = false;
          this.errorMessage = '';
          this.isLoadingNextData = false;
          this.isLoading = false;
          return;
        }
        this.errorMessage = 'Failed to load users. Please try again later.';
        console.error('Error loading users:', error);
      },
      complete: () => {
        this.isLoading = false;
        this.isLoadingNextData = false;
        setTimeout(() => this.updateObserver(), 100);
      }
    });
  }

  getRoleClass(role: string): string {
    return role === 'ROLE_ADMIN' ? 'text-bg-danger' : 'text-bg-success';
  }

  private setupIntersectionObserver(): void {
    this.observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting && this.hasMoreUsers && !this.isLoadingNextData) {
          this.page++;
          this.loadUsers();
        }
      });
    }, {
      root: null,
      rootMargin: '100px',
      threshold: 0.5
    });

    this.updateObserver();
  }

  private updateObserver(): void {
    this.disconnectObserver();

    if (this.lastUserElement?.nativeElement && this.hasMoreUsers) {
      this.observer?.observe(this.lastUserElement.nativeElement);
    }
  }

  private disconnectObserver(): void {
    if (this.observer) {
      this.observer.disconnect();
    }
  }

  onSearch(): void {
    this.searchSubject.next(this.search);
  }

  toggleRole(selectedRole: "ROLE_ADMIN" | "ROLE_CUSTOMER"): void {
    this.role = this.role === selectedRole ? null : selectedRole;
    this.resetAndLoadUsers();
  }

  toggleActivationStatus(status: boolean): void {
    this.enabled = this.enabled === status ? null : status;
    this.resetAndLoadUsers();
  }

  setActivation(id: string, enabled: boolean) {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: enabled ? 'Activate User' : 'Deactivate User',
        message: `Are you sure you want to ${enabled ? 'activate' : 'deactivate'} this user?`
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const userStatus: UserStatus = { userStatus: enabled };
        this.adminService.setActivationUser(id, userStatus).subscribe({
          next: () => {
            this.users = this.users.map(user => {
              if (user.id === id) {
                return { ...user, enabled };
              }
              return user;
            });
          },
          error: (error) => {
            console.error('Error setting activation status:', error);
          }
        });
      }
    });
  }

  setAsAdmin(id: string) {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: 'New admin',
        message: `Are you sure you want to make this user an admin?`
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.adminService.setRole(id, UserRole.ROLE_ADMIN).subscribe({
          next: () => {
            this.users = this.users.map(user => {
              if (user.id === id) {
                return { ...user, role: 'ROLE_ADMIN' };
              }
              return user;
            });
          },
          error: (error) => {
            console.error('Error setting activation status:', error);
          }
        });
      }
    });
  }
}
