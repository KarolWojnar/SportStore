import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CartProductComponent } from '../product/cart-product/cart-product.component';
import { Product } from '../model/product';
import { StoreService } from '../service/store.service';
import { NgForOf, NgIf } from '@angular/common';
import * as bootstrap from 'bootstrap';
import { AuthStateService } from '../service/auth-state.service';

@Component({
  selector: 'app-home',
  imports: [
    RouterLink,
    CartProductComponent,
    NgForOf,
    NgIf
  ],
  standalone: true,
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  products: Product[] = [];
  groupedProducts: Product[][] = [];
  carouselIndicators: number[] = [];
  isLoggedIn = false;

  constructor(private storeService: StoreService,
              private authState: AuthStateService) {
  }

  ngOnInit(): void {
    this.authState.isLoggedIn$.subscribe((isLoggedIn) => {
      this.isLoggedIn = isLoggedIn;
    });
    this.loadFeaturedProducts();
  }

  private loadFeaturedProducts() {
    this.storeService.getFeaturedProducts().subscribe({
      next: (products) => {
        this.products = products.products;
        this.groupedProducts = this.chunkArray(this.products, 3);
        this.carouselIndicators = Array.from({ length: this.groupedProducts.length }, (_, i) => i);
        setTimeout(() => {
          const carouselElement = document.getElementById('productCarousel');
          if (carouselElement) {
            new bootstrap.Carousel(carouselElement, {
              interval: 3000,
              wrap: true
            });
          }
        }, 100);
      },
      error: (error) => {
        console.error('Error fetching featured products:', error);
      }
    });
  }

  private chunkArray(products: Product[], size: number): Product[][] {
    const chunks: Product[][] = [];
    for (let i = 0; i < products.length; i += 3) {
      chunks.push(products.slice(i, i + size));
    }
    return chunks;
  }
}
