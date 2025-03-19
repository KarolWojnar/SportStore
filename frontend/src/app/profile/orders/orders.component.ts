import { Component, OnInit } from '@angular/core';
import { StoreService } from '../../service/store.service';
import { OrderBaseInfo } from '../../model/order';
import { CurrencyPipe, DatePipe, NgClass, NgForOf, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-orders',
  imports: [
    NgClass,
    RouterLink,
    CurrencyPipe,
    DatePipe,
    NgForOf,
    NgIf,
    MatProgressSpinner
  ],
  standalone: true,
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.scss'
})
export class OrdersComponent implements OnInit {

  orders: OrderBaseInfo[] = [];
  isLoading = true;
  isLoadingPayment = false;
  errorMessage: string | null = null;
  payingId = 'XX';

  constructor(private storeService: StoreService) {
  }

  ngOnInit(): void {
    this.storeService.getUserOrders().subscribe({
      next: (response) => {
        this.orders = response.orders;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error fetching orders:', err);
        this.isLoading = false;
      }
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'CREATED':
        return 'created';
      case 'PROCESSING':
        return 'processing';
      case 'SHIPPING':
        return 'shipping';
      case 'DELIVERED':
        return 'delivered';
      case 'ANNULLED':
        return 'annulled';
      case 'REFUNDED':
        return 'returned';
      default:
        return '';
    }
  }

  payForOrder(id: string) {
    if (this.orders.find(order => order.id === id)?.status === 'CREATED') {
      this.isLoadingPayment = true;
      this.payingId = id;
      this.storeService.goToRepayment(id).subscribe({
        next: (response) => {
          this.isLoadingPayment = false;
          this.payingId = 'XX';
          if (response.url) {
            window.location.href = response.url;
          }
        },
        error: (err) => {
          console.error('Error updating customer:', err);
          this.payingId = 'XX';
          this.isLoadingPayment = false;
          this.errorMessage = 'An error occurred while processing payment. Try again later.';
        }
      });
    }
  }
}
