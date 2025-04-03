import {
  Component,
  OnInit,
  AfterViewInit,
  ViewChild,
  ElementRef,
  OnDestroy
} from '@angular/core';
import { AdminService } from '../../service/admin.service';
import { NgClass, NgForOf, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged, Subject, takeUntil } from 'rxjs';
import { ProductDetails, ProductInfo } from '../../model/product';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import { faArrowRotateRight, faArrowUp } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-products',
  imports: [
    NgClass,
    RouterLink,
    NgIf,
    NgForOf,
    FormsModule,
    FaIconComponent
  ],
  standalone: true,
  templateUrl: './products.component.html',
  styleUrl: './products.component.scss'
})
export class ProductsComponent implements OnInit, AfterViewInit, OnDestroy {
  products: ProductDetails[] = [];
  isLoading: boolean = true;
  errorMessage: string | null = null;
  page: number = 0;
  hasMoreProducts: boolean = true;
  isLoadingNextData: boolean = false;
  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();
  categories: string[] = [];
  selectedCategories: string[] = [];
  expandedProductId: string | null = null;
  editedProduct: ProductInfo = {
    id: '',
    name: '',
    price: 0,
    quantity: 0
  };
  protected readonly faArrowUp = faArrowUp;
  protected readonly faArrowRotateRight = faArrowRotateRight;

  @ViewChild('lastProductElement', { static: false }) lastProductElement!: ElementRef;
  search: string = '';
  category: string | null = null;
  private observer: IntersectionObserver | null = null;
  edited = false;
  newCategory = null;
  showForm = false;

  constructor(private adminService: AdminService) { }

  ngOnInit(): void {
    this.setupSearchListener();
    this.loadProducts(true);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.disconnectObserver();
  }

  ngAfterViewInit(): void {
    this.setupIntersectionObserver();
  }

  private setupSearchListener(): void {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.resetAndLoadProducts();
    });
  }

  private resetAndLoadProducts(): void {
    this.page = 0;
    this.products = [];
    this.hasMoreProducts = true;
    this.loadProducts(true);
  }

  loadProducts(initialLoad: boolean = false): void {
    if (this.isLoadingNextData) return;

    initialLoad ? this.isLoading = true : this.isLoadingNextData = true;

    this.adminService.getAllProducts(this.page, this.search, this.selectedCategories).subscribe({
      next: (response) => {
        this.products = [...this.products, ...response.products];
        this.hasMoreProducts = response.products.length === 10;
        this.categories = response.categories ? response.categories : this.categories;
        this.errorMessage = '';
      },
      error: (error) => {
        if (error.status === 404) {
          this.hasMoreProducts = false;
          this.errorMessage = '';
          this.isLoadingNextData = false;
          this.isLoading = false;
          return;
        }
        this.errorMessage = 'Failed to load products. Please try again later.';
        console.error('Error loading products:', error);
      },
      complete: () => {
        this.isLoading = false;
        this.isLoadingNextData = false;
        setTimeout(() => this.updateObserver(), 100);
      }
    });
  }

  getStockClass(quantity: number): string {
    return quantity > 0 ? 'text-bg-success' : 'text-bg-danger';
  }

  private setupIntersectionObserver(): void {
    this.observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting && this.hasMoreProducts && !this.isLoadingNextData) {
          this.page++;
          this.loadProducts();
        }
      });
    }, {
      root: null,
      rootMargin: '100px',
      threshold: 0.5
    });

    this.updateObserver();
  }

  private updateObserver(): void {
    this.disconnectObserver();

    if (this.lastProductElement?.nativeElement && this.hasMoreProducts) {
      this.observer?.observe(this.lastProductElement.nativeElement);
    }
  }

  private disconnectObserver(): void {
    if (this.observer) {
      this.observer.disconnect();
    }
  }

  onSearch(): void {
    this.searchSubject.next(this.search);
  }

  filterByCategory(category: string): void {
    const index = this.selectedCategories.indexOf(category);

    if (index === -1) {
      this.selectedCategories.push(category);
    } else {
      this.selectedCategories.splice(index, 1);
    }
    this.resetAndLoadProducts();
  }

  editProduct(product: ProductDetails) {
    this.edited = true;
    if (this.expandedProductId === product.id) {
      this.cancelEdit();
    } else {
      this.expandedProductId = product.id;
      this.editedProduct = { ...product };
    }
  }

  saveProductChanges(productId: string): void {
    if (this.editedProduct && this.editedProduct.id == productId) {
      this.adminService.updateProduct(productId, this.editedProduct).subscribe({
        next: (response) => {
          console.log('Product updated successfully:', response);
          const index = this.products.findIndex(product => product.id === productId);
          if (index !== -1) {
            this.products[index].name = response.name;
            this.products[index].quantity = response.quantity;
            this.products[index].price = response.price;
          }
          this.cancelEdit();
        },
        error: (error) => {
          console.error('Error updating product:', error);
        }
      });
    }
  }

  cancelEdit(): void {
    this.expandedProductId = null;
    this.editedProduct = {
      id: '',
      name: '',
      price: 0,
      quantity: 0
    };
    this.edited = false;
  }

  scrollToTop(): void {
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
  }

  clearCategoryFilters() {
    this.selectedCategories = [];
    this.resetAndLoadProducts();
  }

  resetData(product: ProductDetails) {
    this.editedProduct = { ...product };
  }

  toggleFormCategory() {
    this.showForm = !this.showForm;
  }

  saveCategory() {
    if (this.newCategory) {
      const categoryNew = {name: this.newCategory}
      this.adminService.addCategory(categoryNew).subscribe({
        next: (response) => {
          this.categories.push(response.name);
          this.newCategory = null;
          this.errorMessage = null;
          this.showForm = false;
        },
        error: (error) => {
          this.errorMessage = error.error.message;
        }
      });
    }
  }

  changeAvailability(productId: string, available: boolean): void {
    this.adminService.changeAvailability(productId, available).subscribe({
      next: () => {
        const index = this.products.findIndex(product => product.id === productId);
        if (index!== -1) {
          this.products[index].available = available;
        }
      },
      error: (error) => {
        console.error('Error updating product availability:', error);
      }
    });
  }
}
