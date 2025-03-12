import { AfterViewInit, Component } from '@angular/core';
import { StoreService } from '../service/store.service';
import { Product } from '../model/product';
import { RouterModule } from '@angular/router';
import { NgbPagination } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import { faChevronDown, faChevronUp, faFilter, faSort, faSortDown, faSortUp, faTimes } from '@fortawesome/free-solid-svg-icons';
import { CurrencyPipe, NgForOf, NgIf } from '@angular/common';
import { CartProductComponent } from './cart-product/cart-product.component';

@Component({
  selector: 'app-product',
  imports: [RouterModule, NgbPagination, FormsModule, FaIconComponent, CurrencyPipe, NgForOf, NgIf, CartProductComponent],
  standalone: true,
  templateUrl: './product.component.html',
  styleUrl: './product.component.scss'
})
export class ProductComponent implements AfterViewInit {

  products: Product[] = [];
  page = 1;
  pageSize = 6;
  totalElements = 0;
  search = '';
  sort = 'id';
  direction = 'asc';
  selectedCategories: string[] = [];
  allCategories: string[] = [];
  showCategories = false;

  faFilter = faFilter;
  faSort = faSort;
  faSortUp = faSortUp;
  faSortDown = faSortDown;
  faTimes = faTimes;
  faChevronDown = faChevronDown;
  faChevronUp = faChevronUp;

  constructor(private storeService: StoreService) {}

  ngAfterViewInit(): void {
    this.getProducts();
    this.loadCategories();
  }

  getProducts(): void {
    this.storeService.getProducts(this.page - 1,
                                  this.pageSize,
                                  this.sort,
                                  this.direction,
                                  this.search,
                                  this.selectedCategories)
      .subscribe(products => {
      this.products = products.products;
      this.totalElements = products.totalElements;
    });
  }

  onPageChange(page: number): void {
    this.page = page;
    this.getProducts();
  }

  sortBy(column: string): void {
    if (this.sort === column) {
      this.direction = this.direction === 'asc' ? 'desc' : 'asc';
    } else {
      this.sort = column;
      this.direction = 'asc';
    }
    this.getProducts();
  }

  onSearch(): void {
    this.page = 1;
    this.getProducts();
  }

  loadCategories(): void {
    this.storeService.getCategories().subscribe(categories => {
      this.allCategories = categories.categories;
    });
  }

  onCategoryChange(category: string, event: Event): void {
    const isChecked = (event.target as HTMLInputElement).checked;
    if (isChecked) {
      this.selectedCategories.push(category);
    } else {
      this.selectedCategories = this.selectedCategories.filter(c => c !== category);
    }
    this.page = 1;
    this.getProducts();
  }

  clearFilters() {
    this.selectedCategories = [];
    this.search = '';
    this.page = 1;
    this.getProducts();
  }

  toggleCategories(): void {
    this.showCategories = !this.showCategories;
  }
}
