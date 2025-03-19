import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { NgIf } from '@angular/common';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import { faArrowRotateRight } from '@fortawesome/free-solid-svg-icons';
import { UserDetails } from '../../model/user-dto';

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
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: [{ value: '', disabled: true }],
      shippingAddress: this.fb.group({
        address: ['', Validators.required],
        city: ['', Validators.required],
        country: ['', Validators.required],
        zipCode: ['', Validators.required]
      })
    });
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
          shippingAddress: {
            address: this.user.shippingAddress.address,
            city: this.user.shippingAddress.city,
            country: this.user.shippingAddress.country,
            zipCode: this.user.shippingAddress.zipCode
          }
        });
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = error.error.message;
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    //todo: submit new data
  }

  protected readonly faArrowRotateRight = faArrowRotateRight;

  resetDataUser() {
    this.profileForm.patchValue({
      firstName: this.user.firstName,
      lastName: this.user.lastName,
      email: this.user.email
    });
  }

  resetDataCustomer() {
    this.profileForm.patchValue({
      shippingAddress: {
        address: this.user.shippingAddress.address,
        city: this.user.shippingAddress.city,
        country: this.user.shippingAddress.country,
        zipCode: this.user.shippingAddress.zipCode
      }
    });
  }
}
