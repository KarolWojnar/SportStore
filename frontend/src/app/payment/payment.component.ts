import { Component, OnDestroy, OnInit } from '@angular/core';
import { StoreService } from '../service/store.service';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { CurrencyPipe, NgIf } from '@angular/common';
import { CustomerDto } from '../model/user-dto';
import { Router, RouterLink } from '@angular/router';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-payment',
  imports: [
    FormsModule,
    CurrencyPipe,
    RouterLink,
    MatProgressSpinner,
    ReactiveFormsModule,
    NgIf
  ],
  standalone: true,
  templateUrl: './payment.component.html',
  styleUrl: './payment.component.scss'
})
export class PaymentComponent implements OnInit, OnDestroy {
  deliveryType: "EXPRESS" | "NORMAL"  = "NORMAL";
  paymentMethod: "CARD" | "BLIK" | "P24" = "CARD";
  paymentProceed = false;
  priceWithDelivery: number = 0;
  customer!: CustomerDto;
  paymentForm: FormGroup;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  isLoading = false;
  constructor(private storeService: StoreService,
              private fb: FormBuilder,
              private router: Router
  ) {
    this.paymentForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      address: ['', Validators.required],
      city: [this.customer?.shippingAddress?.city ||'', Validators.required],
      zipCode: ['', Validators.required],
      country: ['', Validators.required],
      deliveryType: ['NORMAL', Validators.required],
      paymentMethod: ['CARD', Validators.required]
    });
  }

  ngOnDestroy(): void {
    if (!this.paymentProceed) {
      this.cancelPayment();
    }
  }



  ngOnInit(): void {
    this.isLoading = true;
    this.storeService.checkout().subscribe({
      next: (customer) => {
        this.customer = customer.order;
        this.priceWithDelivery = ((this.customer.shippingPrice || 0) + (this.customer.totalPrice || 0));
        if (this.customer.firstName) {
          this.paymentForm.patchValue({
            firstName: this.customer.firstName,
            lastName: this.customer.lastName
          });
          if (this.customer.shippingAddress) {
            this.paymentForm.patchValue({
              address: this.customer.shippingAddress.address,
              city: this.customer.shippingAddress.city,
              zipCode: this.customer.shippingAddress.zipCode,
              country: this.customer.shippingAddress.country
            });
          }
        }
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error fetching customer data:', err);
      }
    });
  }

  proceedToPayment() {
    this.isLoading = true;
    this.paymentProceed = true;

  }

  cancelPayment() {
    this.storeService.cancelPayment().subscribe({
      next: () => {
        this.router.navigate(['/cart']);
      },
      error: (err) => {
        console.error('Error canceling payment:', err);
      }
    });
  }
}
