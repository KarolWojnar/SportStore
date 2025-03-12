import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIf } from '@angular/common';
import { AuthService } from '../../service/auth.service';
import { Router, RouterLink } from '@angular/router';
import { UserLoginDto } from '../../model/user-dto';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    NgIf,
    RouterLink
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  standalone: true
})
export class LoginComponent implements OnInit{
  loginForm: FormGroup;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  email: string | null = null;
  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
    });
  }

  ngOnInit(): void {
    const navigationState = this.router.getCurrentNavigation()?.extras.state;
    if (navigationState && navigationState['email']) {
      this.email = navigationState['email'];
      this.loginForm.patchValue({email: this.email});
    }
    }

  onSubmit() {
    if (this.loginForm.invalid) {
      return;
    }

    const user: UserLoginDto = this.loginForm.value;
    this.authService.login(user).subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.errorMessage = err.error.message || 'Login failed. Please try again.';
      },
    });
  }
}
