import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { CustomerService } from '../../core/services/customer.service';

@Component({
  selector: 'app-member-enroll-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>Enroll New Member</h2>
    <mat-dialog-content>
      <p>Enter customer's mobile number to enroll them as a member.</p>
      <p><strong>Membership Fee: $50.00</strong></p>
      
      <mat-form-field appearance="fill" class="full-width">
        <mat-label>Phone Number</mat-label>
        <input matInput [(ngModel)]="phoneNumber" placeholder="e.g. 555-0123">
      </mat-form-field>
      
      @if (error()) {
        <p class="error">{{ error() }}</p>
      }
      @if (success()) {
        <p class="success">Member enrolled successfully!</p>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Close</button>
      @if (!success()) {
        <button mat-raised-button color="primary" (click)="enroll()" [disabled]="!phoneNumber || loading()">
          {{ loading() ? 'Processing...' : 'Confirm Payment & Enroll' }}
        </button>
      }
    </mat-dialog-actions>
  `,
  styles: [`
    .full-width { width: 100%; }
    .error { color: red; margin-top: 10px; }
    .success { color: green; margin-top: 10px; font-weight: bold; }
  `]
})
export class MemberEnrollDialogComponent {
  private customerService = inject(CustomerService);
  
  restaurantId = 1; // Should be passed in
  phoneNumber = '';
  loading = signal(false);
  error = signal('');
  success = signal(false);

  enroll() {
    this.loading.set(true);
    this.error.set('');
    
    this.customerService.enroll(this.restaurantId, this.phoneNumber).subscribe({
      next: (customer) => {
        this.success.set(true);
        this.loading.set(false);
      },
      error: (err) => {
        if (err.status === 409) {
          this.error.set('Customer is already a member.');
        } else {
          this.error.set('Enrollment failed. Please try again.');
        }
        this.loading.set(false);
      }
    });
  }
}
