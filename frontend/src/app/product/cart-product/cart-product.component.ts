import { Component, Input } from '@angular/core';
import { Product } from '../../model/product';
import { CurrencyPipe, NgForOf, NgIf } from '@angular/common';

@Component({
  selector: 'app-cart-product',
  imports: [
    CurrencyPipe,
    NgForOf,
    NgIf
  ],
  standalone: true,
  templateUrl: './cart-product.component.html',
  styleUrl: './cart-product.component.scss'
})
export class CartProductComponent {
  @Input() product!: Product;
  @Input() isLoggedIn!: boolean;

}
