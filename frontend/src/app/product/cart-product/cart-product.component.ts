import { Component, Input } from '@angular/core';
import { Product } from '../../model/product';
import { CurrencyPipe, NgForOf } from '@angular/common';

@Component({
  selector: 'app-cart-product',
  imports: [
    CurrencyPipe,
    NgForOf
  ],
  standalone: true,
  templateUrl: './cart-product.component.html',
  styleUrl: './cart-product.component.scss'
})
export class CartProductComponent {
  @Input() product!: Product;

}
