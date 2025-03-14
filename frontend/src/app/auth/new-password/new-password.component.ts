import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../service/auth.service';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { NgIf } from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';

@Component({
  selector: 'app-new-password',
  imports: [
    MatProgressSpinner,
    NgIf,
    ReactiveFormsModule
  ],
  standalone: true,
  templateUrl: './new-password.component.html',
  styleUrl: './new-password.component.scss'
})
export class NewPasswordComponent implements OnInit{

  resetForm: FormGroup;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  isLoading = false;
  codeInvalid = false;
  resetCode: string | null = null;
  password: string | null = null;
  confirmPassword: string | null = null;

  constructor(private authService: AuthService,
              private fb: FormBuilder) {
    this.resetForm = this.fb.group({
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(8)]],
      },
      { validators: this.passwordsMatchValidator }
    );
  }

  private passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
      const password = group.get('password')?.value;
      const confirmPassword = group.get('confirmPassword')?.value;
      return password === confirmPassword ? null : { passwordMismatch: true };
    }

  ngOnInit(): void {
    this.checkCode();
  }

  onSubmit() {
    if (this.resetForm.valid) {
      this.isLoading = true;
      this.errorMessage = null;
      this.successMessage = null;
      this.password = this.resetForm.get('password')?.value;
      this.confirmPassword = this.resetForm.get('confirmPassword')?.value;
      if (this.password !== this.confirmPassword || !this.password || !this.confirmPassword || !this.resetCode) {
        this.errorMessage = 'Passwords do not match';
        this.isLoading = false;
        return;
      }
      this.authService.resetPassword(this.password, this.confirmPassword, this.resetCode).subscribe({
        next: () => {
          this.isLoading = false;
          this.successMessage = "Password changed. You can login now!"
          setTimeout(() => {
            window.location.href = '/login';
          }, 3000);
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = error.error.message;
        }
      });
    }
  }

  private checkCode() {
    this.resetCode = window.location.pathname.split('/').pop() || null;
    if (!this.resetCode) {
      this.codeInvalid = true;
    }
    else {
      this.authService.checkResetCode(this.resetCode).subscribe({
        next: (response) => {
          this.codeInvalid = !response;
        },
        error: () => {
          this.codeInvalid = true;
        }
      });
    }
  }
}
