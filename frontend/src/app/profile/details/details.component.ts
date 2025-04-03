import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../service/auth.service';
import { UserDetails } from '../../model/user-dto';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-details',
  imports: [
    NgIf
  ],
  standalone: true,
  templateUrl: './details.component.html',
  styleUrl: './details.component.scss'
})
export class DetailsComponent implements OnInit {

  constructor(private authService: AuthService) { }

  user!: UserDetails;
  errorMessage: string | null = null;
  isLoading = true;

  ngOnInit(): void {
    this.getUserInfo();
  }

  getUserInfo() {
    this.authService.getUserInfo().subscribe({
      next: (response) => {
        this.user = response;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = error.error.message;
        this.isLoading = false;
      }
    });
  }

}
