import { Component, OnInit } from '@angular/core';
import { StoreService } from '../../service/store.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Order, OrderRatingProduct } from '../../model/order';
import { CurrencyPipe, DatePipe, NgClass, NgForOf, NgIf } from '@angular/common';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { ProductCart } from '../../model/product';
import { faStar } from '@fortawesome/free-solid-svg-icons';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';

@Component({
  selector: 'app-order-info',
  imports: [
    CurrencyPipe,
    NgForOf,
    NgClass,
    DatePipe,
    NgIf,
    RouterLink,
    MatProgressSpinner,
    FaIconComponent
  ],
  standalone: true,
  templateUrl: './order-info.component.html',
  styleUrl: './order-info.component.scss'
})
export class OrderInfoComponent implements OnInit {

  order!: Order;
  isLoading = false;
  errorMessage: string | null = null;
  errorMessagePayment: string | null = null;
  timeToDelete: string = '24 hours';
  canRefund: boolean = false;
  rating = 0;
  protected readonly faStar = faStar;
  productRate = 'XX';

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
          if (this.order.status === 'DELIVERED') {
            this.calculateIfCanRefund();
          }
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = err.error.message;
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
      this.errorMessagePayment = null;
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
          this.errorMessagePayment = 'An error occurred while processing payment. Try again later.';
        }
      });
    }
  }

  cancelOrder() {
    this.storeService.cancelOrder(this.order.id).subscribe({
      next: () => {
        this.order.status = 'ANNULLED';
      },
      error: (err) => {
        console.error('Error updating customer:', err);
      }
    });
  }

  private calculateIfCanRefund() {
    const deliveryDate = new Date(this.order.deliveryDate);
    const currentDate = new Date();
    const timeDifference = currentDate.getTime() - deliveryDate.getTime();
    const daysDifference = Math.floor(timeDifference / (1000 * 60 * 60 * 14));
    this.canRefund = daysDifference <= 14;
  }

  rateProduct(number: number) {
    this.rating = number;
  }

  submitRating(product: ProductCart): void {
    if(this.rating !== 0 && this.productRate === product.productId) {
      const rating: OrderRatingProduct = {
        productId: product.productId,
        rating: this.rating,
        orderId: this.order.id
      };
      this.storeService.rateProduct(rating).subscribe({
        next: () => {
          this.rating = 0;
          product.rated = true;
          this.productRate = 'XX';
        },
        error: (err) => {
          this.errorMessage = err.error.message;
          console.error('Error updating customer:', err);
        }
      });
    } else {
      this.productRate = product.productId;
      this.rating = 0;
    }
  }

  refundOrder() {
    this.storeService.refundOrder(this.order.id).subscribe({
      next: () => {
        this.order.status = 'REFUNDED';
      },
      error: (err) => {
        this.errorMessage = err.error.message;
        console.error('Error updating customer:', err);
      }
    });
  }
}
