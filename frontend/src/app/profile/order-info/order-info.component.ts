import { Component, OnInit } from '@angular/core';
import { StoreService } from '../../service/store.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Order } from '../../model/order';
import { CurrencyPipe, DatePipe, NgClass, NgForOf, NgIf } from '@angular/common';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-order-info',
  imports: [
    CurrencyPipe,
    NgForOf,
    NgClass,
    DatePipe,
    NgIf,
    RouterLink,
    MatProgressSpinner
  ],
  standalone: true,
  templateUrl: './order-info.component.html',
  styleUrl: './order-info.component.scss'
})
export class OrderInfoComponent implements OnInit {

  order!: Order;
  isLoading = false;
  errorMessage: string | null = null;
  timeToDelete: string = '24 hours';

  constructor(private storeService: StoreService,
              private route: ActivatedRoute) {
  }

  calculateTimeToDelete(): void {
    const orderDate = new Date(this.order.orderDate);
    const currentDate = new Date();
    const timeDifference = currentDate.getTime() - orderDate.getTime();
    const hoursDifference = Math.floor(timeDifference / (1000 * 60 * 60));

    if (hoursDifference < 24) {
      const remainingHours = 24 - hoursDifference;
      this.timeToDelete = `${remainingHours} hours`;
    } else {
      this.timeToDelete = '24 hours';
    }
  }

  ngOnInit(): void {
    const orderId = this.route.snapshot.paramMap.get('id');
    if (orderId) {
      this.storeService.getOrderById(orderId).subscribe({
        next: (response) => {
          this.order = response.order;
          if (this.order.status === 'CREATED') {
            this.calculateTimeToDelete();
          }
        },
        error: (err) => {
          console.error('Error fetching order:', err);
        }
      });
    }
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

  payForOrder() {
    if (this.order.status === 'CREATED') {
      this.isLoading = true;
      this.storeService.goToRepayment(this.order.id).subscribe({
        next: (response) => {
          this.isLoading = false;
          if (response.url) {
            window.location.href = response.url;
          }
        },
        error: (err) => {
          console.error('Error updating customer:', err);
          this.isLoading = false;
          this.errorMessage = 'An error occurred while processing payment. Try again later.';
        }
      });
    }
  }
}
