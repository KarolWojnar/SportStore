import { Component, OnInit } from '@angular/core';
import { ProductCart } from '../model/product';
import { StoreService } from '../service/store.service';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { Router, RouterLink } from '@angular/router';
import { CurrencyPipe, NgForOf, NgIf } from '@angular/common';
import { faTrash, faPlus, faMinus, faShoppingCart } from '@fortawesome/free-solid-svg-icons';
import { AuthStateService } from '../service/auth-state.service';

@Component({
  selector: 'app-cart',
  imports: [NgForOf, NgIf, CurrencyPipe, RouterLink, FontAwesomeModule],
  standalone: true,
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.scss'
})
export class CartComponent implements OnInit {

  faTrash = faTrash;
  faPlus = faPlus;
  faMinus = faMinus;
  products: ProductCart[] = [];
  totalPrice = 0;
  errorMessage: string | null = null;

  constructor(private storeService: StoreService,
              private authState: AuthStateService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.storeService.getCart().subscribe({
      next: (data) => {
        this.products = data.products;
        this.calculateTotalPrice();
      },
      error: (err) => {
        console.error('Error fetching cart:', err);
      }
    });
  }

  calculateTotalPrice(): void {
    this.totalPrice = this.products.reduce((total, item) => {
      return total + (item.price * item.quantity);
    }, 0);
    this.totalPrice = parseFloat(this.totalPrice.toFixed(2));
  }

  increaseQuantity(productId: string): void {
    this.storeService.addToCart(productId).subscribe({
      next: () => {
        this.loadCart();
      },
      error: (error) => {
        console.error('Error increasing quantity:', error);
      }
    });
  }

  decreaseQuantity(productId: string): void {
    this.storeService.removeOneFromCart(productId).subscribe({
      next: () => {
        this.loadCart();
      },
      error: (error) => {
        console.error('Error decreasing quantity:', error);
      }
    });
  }

  removeItem(productId: string): void {
    this.storeService.removeProduct(productId).subscribe({
      next: () => {
        this.loadCart();
      },
      error: (error) => {
        console.error('Error removing item:', error);
      }
    });
  }

  clearCart(): void {
    this.storeService.clearCart().subscribe({
      next: () => {
        this.loadCart();
      },
      error: (error) => {
        console.error('Error clearing cart:', error);
      }
    });
  }

  loadCart(): void {
    this.storeService.getCart().subscribe({
      next: (response) => {
        this.products = response.products;
        if (this.products.length === 0) {
          this.authState.setCartHasItems(false);
          localStorage.setItem('cartHasItems', 'false');
        }
        this.calculateTotalPrice();
      },
      error: (error) => {
        console.error('Error loading cart:', error);
      }
    });
  }

  protected readonly faShoppingCart = faShoppingCart;

  validAndNavigateToCheckout() {
    if (this.products.length > 0) {
      this.storeService.validCart().subscribe({
        next: () => {
          this.router.navigate(['/checkout']);
        },
        error: (error) => {
          if (error.error.message === 'Order is already processing.') {
            this.router.navigate(['/checkout']);
          }
          this.errorMessage = error.error.message;
        }
      });
    }
  }
}
