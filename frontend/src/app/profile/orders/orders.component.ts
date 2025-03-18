import { Component, OnInit } from '@angular/core';
import { StoreService } from '../../service/store.service';
import { OrderBaseInfo } from '../../model/order';
import { CurrencyPipe, DatePipe, NgClass, NgForOf, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-orders',
  imports: [
    NgClass,
    RouterLink,
    CurrencyPipe,
    DatePipe,
    NgForOf,
    NgIf
  ],
  standalone: true,
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.scss'
})
export class OrdersComponent implements OnInit {

  orders: OrderBaseInfo[] = [];
  isLoading = true;

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

}
