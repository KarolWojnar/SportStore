import {
  Component,
  OnInit,
  AfterViewInit,
  ViewChild,
  ElementRef,
  OnDestroy
} from '@angular/core';
import { AdminService } from '../../service/admin.service';
import { DatePipe, NgClass, NgForOf, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged, Subject, takeUntil } from 'rxjs';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import { faArrowUp } from '@fortawesome/free-solid-svg-icons';
import { OrderBaseInfo } from '../../model/order';

@Component({
  selector: 'app-orders',
  imports: [
    NgClass,
    RouterLink,
    NgIf,
    NgForOf,
    FormsModule,
    FaIconComponent,
    DatePipe
  ],
  standalone: true,
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.scss'
})
export class OrdersComponent implements OnInit, AfterViewInit, OnDestroy {
  orders: OrderBaseInfo[] = [];
  isLoading: boolean = true;
  errorMessage: string | null = null;
  page: number = 0;
  status: "CREATED" | "PROCESSING" | "SHIPPING" | "DELIVERED" | "ANNULLED" | "REFUNDED" | null = null;
  hasMoreOrders: boolean = true;
  isLoadingNextData: boolean = false;
  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  orderStatuses = ['CREATED', 'PROCESSING', 'SHIPPING', 'DELIVERED', 'ANNULLED', 'REFUNDED'];
  selectedStatus: string | null = null;
  search: string = '';

  protected readonly faArrowUp = faArrowUp;

  @ViewChild('lastOrderElement', { static: false }) lastOrderElement!: ElementRef;
  private observer: IntersectionObserver | null = null;

  constructor(private adminService: AdminService) { }

  ngOnInit(): void {
    this.setupSearchListener();
    this.loadOrders(true);
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
      this.resetAndLoadOrders();
    });
  }

  private resetAndLoadOrders(): void {
    this.page = 0;
    this.orders = [];
    this.hasMoreOrders = true;
    this.loadOrders(true);
  }

  loadOrders(initialLoad: boolean = false): void {
    if (this.isLoadingNextData) return;

    initialLoad ? this.isLoading = true : this.isLoadingNextData = true;

    this.adminService.getOrders(this.page, this.selectedStatus).subscribe({
      next: (response) => {
        this.orders = [...this.orders, ...response];
        this.hasMoreOrders = response.length === 10;
        this.errorMessage = '';
      },
      error: (error) => {
        if (error.status === 404) {
          this.hasMoreOrders = false;
          this.errorMessage = '';
          this.isLoadingNextData = false;
          this.isLoading = false;
          return;
        }
        this.errorMessage = 'Failed to load orders. Please try again later.';
        console.error('Error loading orders:', error);
      },
      complete: () => {
        this.isLoading = false;
        this.isLoadingNextData = false;
        setTimeout(() => this.updateObserver(), 100);
      }
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'CREATED':
        return 'bg-primary';
      case 'PROCESSING':
        return 'bg-warning text-dark';
      case 'SHIPPING':
        return 'bg-info text-dark';
      case 'DELIVERED':
        return 'bg-success';
      case 'ANNULLED':
        return 'bg-danger';
      case 'REFUNDED':
        return 'bg-secondary';
      default:
        return 'bg-light text-dark';
    }
  }

  private setupIntersectionObserver(): void {
    this.observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting && this.hasMoreOrders && !this.isLoadingNextData) {
          this.page++;
          this.loadOrders();
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

    if (this.lastOrderElement?.nativeElement && this.hasMoreOrders) {
      this.observer?.observe(this.lastOrderElement.nativeElement);
    }
  }

  private disconnectObserver(): void {
    if (this.observer) {
      this.observer.disconnect();
    }
  }

  cancelOrder(orderId: string): void {
    if (confirm('Are you sure you want to cancel this order?')) {
      this.adminService.cancelOrder(orderId).subscribe({
        next: () => {
          const order = this.orders.find(o => o.id === orderId);
          if (order) {
            order.status = 'ANNULLED';
          }
        },
        error: (error) => {
          console.error('Error cancelling order:', error);
          this.errorMessage = 'Failed to cancel order. Please try again.';
        }
      });
    }
  }

  scrollToTop(): void {
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
  }

  filterByStatus(status: string): void {
    if (this.selectedStatus === status) {
      this.selectedStatus = null;
    } else {
    this.selectedStatus = status;
    }
    this.resetAndLoadOrders();
  }

  clearStatusFilters() {
    this.selectedStatus = null;
    this.resetAndLoadOrders();
  }
}
