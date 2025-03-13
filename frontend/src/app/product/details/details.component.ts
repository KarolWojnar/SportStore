import { Component, OnInit } from '@angular/core';
import { Product } from '../../model/product';
import { StoreService } from '../../service/store.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CurrencyPipe, NgForOf, NgIf } from '@angular/common';
import { AuthStateService } from '../../service/auth-state.service';

@Component({
  selector: 'app-details',
  imports: [
    CurrencyPipe,
    NgIf,
    NgForOf,
    RouterLink
  ],
  standalone: true,
  templateUrl: './details.component.html',
  styleUrl: './details.component.scss'
})
export class DetailsComponent implements OnInit {

  product!: Product;
  relatedProducts!: Product[];
  isLoggedIn = false;

  constructor(private storeService: StoreService,
              private route: ActivatedRoute,
              private authState: AuthStateService) { }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const productId = params.get('id');
      if (productId) {
        this.loadProductDetails(productId);
      }
    });

    this.authState.isLoggedIn$.subscribe({
      next: (isLoggedIn) => {
        this.isLoggedIn = isLoggedIn;
      }
    });
  }

  private loadProductDetails(productId: string) {
    this.storeService.getProductDetails(productId).subscribe({
      next: (product) => {
        this.product = product.product;
        this.relatedProducts = product.relatedProducts;
      },
      error: (error) => {
        console.error('Error fetching product:', error);
      }
    });
  }

  addToCart(id: string, imgElement: HTMLImageElement, event: Event) {
    event.stopPropagation();
    if (this.isLoggedIn) {
      this.storeService.sendRequest(id, imgElement);
    }
  }
}
