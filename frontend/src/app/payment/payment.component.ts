import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { StoreService } from '../service/store.service';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { CurrencyPipe, NgClass, NgForOf, NgIf } from '@angular/common';
import { CustomerDto } from '../model/user-dto';
import { Router, RouterLink } from '@angular/router';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import {
  faCreditCard,
  faMobile,
  faMoneyBill,
  faMoneyBill1,
  faWallet
} from '@fortawesome/free-solid-svg-icons';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-payment',
  imports: [
    FormsModule,
    CurrencyPipe,
    RouterLink,
    MatProgressSpinner,
    ReactiveFormsModule,
    NgIf,
    NgForOf,
    FaIconComponent,
    NgClass
  ],
  standalone: true,
  templateUrl: './payment.component.html',
  styleUrl: './payment.component.scss'
})
export class PaymentComponent implements OnInit, OnDestroy {
  deliveryType: "EXPRESS" | "STANDARD"  = "STANDARD";
  paymentMethods = [
    { id: 'CARD', label: 'Credit/Debit Card', icon: faCreditCard },
    { id: 'P24', label: 'Przelewy24', icon: faMoneyBill },
    { id: 'PAYPAL', label: 'PayPal', icon: faMoneyBill1 },
    { id: 'REVOLUT_PAY', label: 'Revolut Pay', icon: faWallet},
    { id: 'MOBILEPAY', label: 'MobilePay', icon: faMobile }
  ];
  paymentProceed = false;
  priceWithDelivery: number = 0;
  customer!: CustomerDto;
  paymentForm: FormGroup;
  isDarkMode = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  errorFormMessage: string | null = null;
  isLoading = false;
  price = 0;
  @ViewChild('confirmationModal') confirmationModal!: TemplateRef<any>;

  constructor(private storeService: StoreService,
              private fb: FormBuilder,
              private router: Router,
              private modalService: NgbModal
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

  openConfirmationModal(content: TemplateRef<any>) {
    this.modalService.open(content, { ariaLabelledBy: 'modal-basic-title' }).result.then(
      (result) => {
        console.log('Modal closed with result:', result);
      },
      (reason) => {
        console.log('Modal dismissed with reason:', reason);
      }
    );
  }

  checkPrice() {
    this.storeService.getTotalPrice().subscribe({
      next: (totalPrice) => {
        this.priceWithDelivery = totalPrice.totalPrice;
        this.price = totalPrice.totalPrice;
      },
      error: (err) => {
        console.error('Error fetching total price:', err);
      }
    });
  }

  loadPaymentData() {
    const storedCustomer = localStorage.getItem('customer');
    if (storedCustomer) {
      this.customer = JSON.parse(storedCustomer);
      this.setValues();
    } else {
      this.isLoading = true;
      this.storeService.checkout().subscribe({
        next: (customer) => {
          this.customer = customer.order;
          localStorage.setItem('customer', JSON.stringify(customer.order));
          this.setValues();
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error fetching customer data:', err);
        }
      });
    }
  }

  setValues() {
    this.priceWithDelivery = ((this.customer.shippingPrice || 0.0) + (this.customer.totalPrice || 0));
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
  }


  ngOnInit(): void {
    this.isDarkMode = localStorage.getItem('isDarkMode') === 'true';
    this.loadPaymentData();
    this.checkPrice();
    this.addDeliveryTypeListener();
  }

  confirmPayment(modal: any) {
    if (this.paymentForm.valid) {
      modal.close();
      this.proceedToPayment();
    } else {
      this.errorFormMessage = 'Please fill in all required fields.';
    }
  }

  pay() {
    if (this.paymentForm.valid) {
      this.openConfirmationModal(this.confirmationModal);
    } else {
      this.errorFormMessage = 'Please fill in all required fields.';
    }
  }

  proceedToPayment() {
    if (this.paymentForm.valid) {
      this.isLoading = true;
      this.paymentProceed = true;
      this.errorFormMessage = null;
      this.errorMessage = null;

      this.customer.firstName = this.paymentForm.value.firstName;
      this.customer.lastName = this.paymentForm.value.lastName;
      this.customer.deliveryTime = this.deliveryType;
      this.customer.paymentMethod = this.paymentForm.value.paymentMethod;
      this.customer.shippingAddress = {
        address: this.paymentForm.value.address,
        city: this.paymentForm.value.city,
        zipCode: this.paymentForm.value.zipCode,
        country: this.paymentForm.value.country
      };
      this.storeService.goToPayment(this.customer).subscribe({
        next: (response) => {
          this.isLoading = false;
          if (response.url) {
            localStorage.removeItem('customer');
            localStorage.removeItem('cartHasItems');
            window.location.href = response.url;
          }
        },
        error: (err) => {
          console.error('Error updating customer:', err);
          this.isLoading = false;
          this.errorMessage = 'An error occurred while processing payment. Try again later.';
        }
      });
    } else {
      this.errorFormMessage = 'Please fill in all required fields.';
    }

  }

  cancelPayment() {
    this.storeService.cancelPayment().subscribe({
      next: () => {
        localStorage.removeItem('customer');
        this.router.navigate(['/cart']);
      },
      error: (err) => {
        console.error('Error canceling payment:', err);
      }
    });
  }

  private addDeliveryTypeListener() {
    this.paymentForm.get('deliveryType')?.valueChanges.subscribe((value) => {
      this.deliveryType = value;
      this.customer.deliveryTime = value === 'EXPRESS' ? 'EXPRESS' : 'STANDARD';
      this.priceWithDelivery = this.deliveryType === 'EXPRESS' ? (this.customer.totalPrice || 0) + 10 : (this.customer.totalPrice || 0);
    });
  }
}
