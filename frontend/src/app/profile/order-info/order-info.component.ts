import { Component, OnInit } from '@angular/core';
import { StoreService } from '../../service/store.service';
import { ActivatedRoute } from '@angular/router';
import { Order } from '../../model/order';
import { CurrencyPipe, DatePipe, NgClass, NgForOf, NgIf } from '@angular/common';

@Component({
  selector: 'app-order-info',
  imports: [
    CurrencyPipe,
    NgForOf,
    NgClass,
    DatePipe,
    NgIf
  ],
  standalone: true,
  templateUrl: './order-info.component.html',
  styleUrl: './order-info.component.scss'
})
export class OrderInfoComponent implements OnInit {

  order!: Order;

  constructor(private storeService: StoreService,
              private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    const orderId = this.route.snapshot.paramMap.get('id');
    if (orderId) {
      this.storeService.getOrderById(orderId).subscribe({
        next: (response) => {
          this.order = response.order;
          console.log(this.order);
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

}
