import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { NgIf } from '@angular/common';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import { faArrowRotateRight } from '@fortawesome/free-solid-svg-icons';
import { CustomerDetails, UserDetails } from '../../model/user-dto';

@Component({
  selector: 'app-edit',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    NgIf,
    FaIconComponent
  ],
  standalone: true,
  templateUrl: './edit.component.html',
  styleUrl: './edit.component.scss'
})
export class EditComponent implements OnInit {
  profileForm: FormGroup;
  isLoading = false;
  isSaving = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  user!: UserDetails;
  customerDto!: CustomerDetails;
  protected readonly faArrowRotateRight = faArrowRotateRight;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService
  ) {
    this.profileForm = this.createForm();
  }

  ngOnInit(): void {
    this.loadUserProfile();
  }

  createForm(): FormGroup {
    return this.fb.group({
      firstName: ['', Validators.maxLength(50)],
      lastName: ['', Validators.maxLength(50)],
      email: [{ value: '', disabled: true }],
      shippingAddress: this.fb.group({
        address: ['', Validators.maxLength(50)],
        city: ['', Validators.maxLength(50)],
        country: ['', Validators.maxLength(50)],
        zipCode: ['', Validators.maxLength(50)]
      })
    });
  }

  isSomethingChanged(): boolean {
    return this.profileForm.value.firstName !== this.user.firstName ||
      this.profileForm.value.lastName !== this.user.lastName ||
      this.profileForm.value.shippingAddress.address !== this.user.shippingAddress?.address ||
      this.profileForm.value.shippingAddress.city !== this.user.shippingAddress?.city ||
      this.profileForm.value.shippingAddress.country !== this.user.shippingAddress?.country ||
      this.profileForm.value.shippingAddress.zipCode !== this.user.shippingAddress?.zipCode;

  }

  loadUserProfile(): void {
    this.isLoading = true;
    this.authService.getUserInfo().subscribe({
      next: (response) => {
        this.user = response.user;
        this.profileForm.patchValue({
          firstName: this.user.firstName,
          lastName: this.user.lastName,
          email: this.user.email,
        });
        if (this.user.shippingAddress) {
          this.profileForm.get('shippingAddress')?.patchValue({
            address: this.user.shippingAddress.address,
            city: this.user.shippingAddress.city,
            country: this.user.shippingAddress.country,
            zipCode: this.user.shippingAddress.zipCode
          });
        }
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = error.error.message;
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.profileForm.valid && this.isSomethingChanged()) {
      this.isSaving = true;
      this.customerDto = this.profileForm.value;
      this.authService.updateCustomerInfo(this.customerDto).subscribe({
        next: (response) => {
          this.successMessage = 'New data saved!';
          this.user = response.user;
          this.isSaving = false;
        },
        error: (error) => {
          this.errorMessage = error.error.message;
          this.isSaving = false;
        }
      });
      setTimeout(() => {
        this.successMessage = null;
        this.errorMessage = null;
      }, 3000);
    }
  }

  resetDataUser() {
    this.profileForm.patchValue({
      firstName: this.user.firstName? this.user.firstName : null,
      lastName: this.user.lastName? this.user.lastName : null,
      email: this.user.email? this.user.email : null
    });
  }

  resetDataCustomer() {
    this.profileForm.patchValue({
      shippingAddress: {
        address: this.user.shippingAddress?.address? this.user.shippingAddress.address : null,
        city: this.user.shippingAddress?.city? this.user.shippingAddress.city : null,
        country: this.user.shippingAddress?.country? this.user.shippingAddress.country : null,
        zipCode: this.user.shippingAddress?.zipCode? this.user.shippingAddress.zipCode : null
      }
    });
  }
}
