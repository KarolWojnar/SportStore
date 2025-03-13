import { Component, Input } from '@angular/core';
import { Product } from '../../model/product';
import { CurrencyPipe, NgForOf, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { StoreService } from '../../service/store.service';
import { HighlightPipe } from '../../service/highlightPipe';

@Component({
  selector: 'app-cart-product',
  imports: [
    CurrencyPipe,
    NgForOf,
    NgIf,
    RouterLink,
    HighlightPipe
  ],
  standalone: true,
  templateUrl: './cart-product.component.html',
  styleUrl: './cart-product.component.scss'
})
export class CartProductComponent {
  @Input() product!: Product;
  @Input() isLoggedIn!: boolean;
  @Input() search!: string;

  constructor(private storeService: StoreService) {
  }

  addToCart(id: string, imgElement: HTMLImageElement, event: Event) {
    event.stopPropagation();
    if (this.isLoggedIn) {
      this.storeService.sendRequest(id, imgElement);
    }
  }
}
