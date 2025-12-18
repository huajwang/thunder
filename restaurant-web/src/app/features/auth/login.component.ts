import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <mat-card-header>
          <mat-card-title>Staff Login</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <form (ngSubmit)="login()">
            <mat-form-field appearance="fill" class="full-width">
              <mat-label>Username</mat-label>
              <input matInput [(ngModel)]="username" name="username" required>
            </mat-form-field>
            
            <mat-form-field appearance="fill" class="full-width">
              <mat-label>Password</mat-label>
              <input matInput type="password" [(ngModel)]="password" name="password" required>
            </mat-form-field>

            @if (error()) {
              <p class="error-message">{{ error() }}</p>
            }

            <button mat-raised-button color="primary" type="submit" [disabled]="loading()">
              {{ loading() ? 'Logging in...' : 'Login' }}
            </button>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 100vh;
      background-color: #f5f5f5;
    }
    .login-card {
      width: 100%;
      max-width: 400px;
      padding: 20px;
    }
    .full-width {
      width: 100%;
      margin-bottom: 15px;
    }
    .error-message {
      color: red;
      margin-bottom: 15px;
    }
    button {
      width: 100%;
    }
  `]
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  username = '';
  password = '';
  loading = signal(false);
  error = signal('');

  login() {
    if (!this.username || !this.password) return;

    this.loading.set(true);
    this.error.set('');

    this.authService.login(this.username, this.password).subscribe({
      next: (response) => {
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
        this.router.navigateByUrl(returnUrl);
      },
      error: () => {
        this.error.set('Invalid credentials');
        this.loading.set(false);
      }
    });
  }
}
