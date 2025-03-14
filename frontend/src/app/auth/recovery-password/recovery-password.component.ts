import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../service/auth.service';
import { Router } from '@angular/router';
import { NgIf } from '@angular/common';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-recovery-password',
  imports: [
    ReactiveFormsModule,
    NgIf,
    MatProgressSpinner
  ],
  standalone: true,
  templateUrl: './recovery-password.component.html',
  styleUrl: './recovery-password.component.scss'
})
export class RecoveryPasswordComponent {

  loginForm: FormGroup;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  email: string | null = null;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit() {
    this.isLoading = true;
    this.errorMessage = null;
    this.successMessage = null;
    if (this.loginForm.valid) {
      const email: string = this.loginForm.get('email')?.value;
      this.authService.recoveryPassword(email).subscribe({
        next: (res) => {
          this.isLoading = false;
          this.successMessage = 'Email sent successfully. Please check your email.';
          setTimeout(() => {
            this.successMessage = null;
            this.router.navigate(['/login'])
          }, 5000);
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = err.error.message || 'Email not sent. Please try again.';
        }
      });
      }
  }
}
