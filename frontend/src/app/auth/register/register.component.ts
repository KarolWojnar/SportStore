import { Component } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import { AuthService } from '../../service/auth.service';
import { Router, RouterLink } from '@angular/router';
import { UserDto } from '../../model/user-dto';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    NgIf,
    RouterLink
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
  standalone: true,
})
export class RegisterComponent {
  registerForm: FormGroup;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(8)]],
      firstName: [''],
      lastName: [''],
      shippingAddress: [''],
    },
      { validators: this.passwordsMatchValidator }
    );
  }

  private passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  onSubmit() {
    if (this.registerForm.invalid) {
      return;
    }

    const user: UserDto = this.registerForm.value;
    this.authService.registerUser(user).subscribe({
      next: (res) => {
        this.registerForm.reset();
        this.errorMessage = null;
        this.successMessage = 'Registration successful. Please confirm your account by email.';
        setTimeout(() => {
          this.successMessage = null;
          this.router.navigate(['/login'])
        }, 5000);
      },
      error: (err) => {
        if (err.error.details?.passwordMatching) {
          this.registerForm.setErrors({ passwordMismatch: true });
        }
        this.errorMessage = err.error.details.toString() || 'Registration failed. Please try again.';
      },
    });
  }
}
