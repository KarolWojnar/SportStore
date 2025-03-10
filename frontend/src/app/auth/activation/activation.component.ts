import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-activation',
  imports: [
    NgIf
  ],
  standalone: true,
  templateUrl: './activation.component.html',
  styleUrl: './activation.component.scss'
})
export class ActivationComponent implements OnInit{
  activationToken: string | null = null;
  activationMessage: string | null = null;
  errorMessage: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.activationToken = this.route.snapshot.paramMap.get('activationCode');
    if (this.activationToken) {
      this.activateAccount(this.activationToken);
    } else {
      this.errorMessage = 'Invalid activation link.';
    }
  }

  activateAccount(token: string) {
    this.authService.activateAccount(token).subscribe({
      next: (res) => {
        this.activationMessage = 'Account activated successfully! You can now log in.';
        this.errorMessage = null;
        setTimeout(() => {
          this.router.navigate(['/login'], {state:{email: res} });
        }, 3000);
      },
      error: (err) => {
        err.error.message
        this.errorMessage = err.error.message || 'Activation failed. Please try again.';
        this.activationMessage = null;
      },
    });
  }
}
