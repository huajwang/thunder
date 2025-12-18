import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { CustomerService } from '../../core/services/customer.service';

@Component({
  selector: 'app-member-login-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>Member Login</h2>
    <mat-dialog-content>
      @if (!codeSent()) {
        <p>Enter your mobile number to receive a login code.</p>
        <mat-form-field appearance="fill" class="full-width">
          <mat-label>Phone Number</mat-label>
          <input matInput [(ngModel)]="phoneNumber" placeholder="e.g. 555-0123">
        </mat-form-field>
      } @else {
        <p>Enter the code sent to {{ phoneNumber }}. (Use 123456)</p>
        <mat-form-field appearance="fill" class="full-width">
          <mat-label>Verification Code</mat-label>
          <input matInput [(ngModel)]="code" placeholder="123456">
        </mat-form-field>
      }
      
      @if (error()) {
        <p class="error">{{ error() }}</p>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      @if (!codeSent()) {
        <button mat-raised-button color="primary" (click)="sendCode()" [disabled]="!phoneNumber || loading()">
          {{ loading() ? 'Sending...' : 'Send Code' }}
        </button>
      } @else {
        <button mat-raised-button color="primary" (click)="login()" [disabled]="!code || loading()">
          {{ loading() ? 'Verifying...' : 'Login' }}
        </button>
      }
    </mat-dialog-actions>
  `,
  styles: [`
    .full-width { width: 100%; }
    .error { color: red; margin-top: 10px; }
  `]
})
export class MemberLoginDialogComponent {
  private customerService = inject(CustomerService);
  private dialogRef = inject(MatDialogRef<MemberLoginDialogComponent>);

  // In a real app, pass restaurantId via MAT_DIALOG_DATA
  restaurantId = 1; // Hardcoded for now or passed in
  
  phoneNumber = '';
  code = '';
  codeSent = signal(false);
  loading = signal(false);
  error = signal('');

  sendCode() {
    this.loading.set(true);
    this.error.set('');
    this.customerService.requestLoginCode(this.restaurantId, this.phoneNumber).subscribe({
      next: () => {
        this.codeSent.set(true);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Failed to send code.');
        this.loading.set(false);
      }
    });
  }

  login() {
    this.loading.set(true);
    this.error.set('');
    this.customerService.login(this.restaurantId, this.phoneNumber, this.code).subscribe({
      next: (customer) => {
        this.dialogRef.close(customer);
      },
      error: (err) => {
        this.error.set('Invalid code or login failed.');
        this.loading.set(false);
      }
    });
  }
}
