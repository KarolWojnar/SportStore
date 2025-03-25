import { Component, Inject } from '@angular/core';
import {
  MAT_DIALOG_DATA, MatDialogActions, MatDialogContent,
  MatDialogRef, MatDialogTitle
} from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';
import { MatButton } from '@angular/material/button';
import { NgForOf, NgIf } from '@angular/common';

interface CategoryDialogData {
  title: string;
  message: string;
  categories: string[];
}

@Component({
  selector: 'app-category-dialog',
  imports: [
    FormsModule,
    MatDialogActions,
    MatButton,
    NgIf,
    NgForOf,
    MatDialogContent,
    MatDialogTitle
  ],
  templateUrl: './category-dialog.component.html',
  standalone: true,
  styleUrl: './category-dialog.component.scss'
})

export class CategoryDialogComponent {
  categoryName: string = '';
  error: string = '';

  constructor(
    public dialogRef: MatDialogRef<CategoryDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: CategoryDialogData,
  ) {}

  addCategory(): void {
    if (!this.categoryName) {
      this.error = 'Category name cannot be empty';
      return;
    }

    if (this.data.categories && this.data.categories.includes(this.categoryName)) {
      this.error = 'This category already exists';
      return;
    }

    this.dialogRef.close(this.categoryName);
  }
}
