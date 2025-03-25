import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminService } from '../../../service/admin.service';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { NgForOf, NgIf } from '@angular/common';
import { NewProduct } from '../../../model/product';
import { StoreService } from '../../../service/store.service';

@Component({
  selector: 'app-new',
  imports: [
    NgIf,
    NgForOf,
    ReactiveFormsModule,
    RouterLink
  ],
  standalone: true,
  templateUrl: './new.component.html',
  styleUrl: './new.component.scss'
})
export class NewComponent implements OnInit {

  productForm: FormGroup;
  newProduct!: NewProduct;
  selectedFile: File | null = null;
  isLoading = false;
  categories: string[] = [];
  errorMessage: string | null = null;
  successMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private adminService: AdminService,
    private router: Router,
    private storeService: StoreService
  ) {
    this.productForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      price: ['', [Validators.required, Validators.min(0.01)]],
      quantity: ['', [Validators.required, Validators.min(0)]],
      categories: [[]]
    });
  }

  ngOnInit(): void {
    this.getCategories();
  }

  getCategories() {
    this.storeService.getCategories().subscribe({
      next: (data) => {
        this.categories = data.categories;
      },
      error: (error) => {
        console.error('Error fetching categories:', error);
      }
    });
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file && file.type === 'image/png') {
      if (file.size > 1024 * 1024 * 10) {
        this.errorMessage = 'File size exceeds 10MB.';
        this.selectedFile = null;
        return;
      } else{
        this.selectedFile = file;
        this.errorMessage = null;
      }
    } else {
      this.errorMessage = 'Please select a PNG image file.';
      this.selectedFile = null;
    }
  }

  onSubmit(): void {
    if (this.productForm.invalid) {
      this.errorMessage = 'Please fill all required fields correctly.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;
    this.successMessage = null;

    this.newProduct = this.productForm.value;

    const formData = new FormData();
    formData.append('product', JSON.stringify(this.newProduct));
    if (this.selectedFile) {
      formData.append('file', this.selectedFile);
    }

    this.adminService.addProduct(formData).subscribe({
      next: () => {
        this.successMessage = 'Product created successfully!';
        setTimeout(() => this.router.navigate(['/admin/products']), 1500);
      },
      error: (error: HttpErrorResponse) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Failed to create product. Please try again.';
        console.error('Error creating product:', error);
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  get name() { return this.productForm.get('name'); }
  get price() { return this.productForm.get('price'); }
  get quantity() { return this.productForm.get('quantity'); }
  get description() { return this.productForm.get('description'); }

  updateCategories(category: string, $event: Event) {
    const isChecked = ($event.target as HTMLInputElement).checked;
    const categories = this.productForm.get('categories')?.value || [];
    if (isChecked) {
      categories.push(category);
    } else {
      const index = categories.indexOf(category);
      if (index !== -1) {
        categories.splice(index, 1);
      }
    }
  }
}
