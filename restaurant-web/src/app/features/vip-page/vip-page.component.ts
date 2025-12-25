import { Component, inject, Input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { MenuItem } from '../../core/models/restaurant.types';
import { RestaurantService } from '../../core/services/restaurant.service';
import { MemberLoginDialogComponent } from '../customer/member-login-dialog.component';

@Component({
  selector: 'app-vip-page',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatCardModule, RouterModule, MatDialogModule],
  template: `
    <div class="vip-container">
      <div class="vip-header">
        <h1>Become a VIP Member</h1>
        <p>Unlock exclusive benefits and rewards!</p>
      </div>

      <div class="benefits-grid">
        <mat-card class="benefit-card">
          <mat-card-header>
            <mat-icon mat-card-avatar color="primary">percent</mat-icon>
            <mat-card-title>10% Off Every Order</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <p>Enjoy a permanent 10% discount on all your orders, dine-in or takeout.</p>
          </mat-card-content>
        </mat-card>

        <mat-card class="benefit-card">
          <mat-card-header>
            <mat-icon mat-card-avatar color="accent">cake</mat-icon>
            <mat-card-title>Birthday Special</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <p>Get a free dessert or appetizer on your birthday week.</p>
          </mat-card-content>
        </mat-card>

        <mat-card class="benefit-card">
          <mat-card-header>
            <mat-icon mat-card-avatar color="warn">event_seat</mat-icon>
            <mat-card-title>Priority Seating</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <p>Skip the line! VIP members get priority access to table reservations.</p>
          </mat-card-content>
        </mat-card>
      </div>

      <div class="action-section">
        @if (loading()) {
          <p>Loading membership details...</p>
        } @else if (error()) {
          <p class="error">{{ error() }}</p>
          <button mat-button (click)="goBack()">Back to Menu</button>
        } @else {
          <div class="price-tag">
            <span class="label">Membership Fee</span>
            <span class="amount">\${{ vipItem.price.toFixed(2) }}</span>
            <span class="period">/ year</span>
          </div>
          
          <button mat-raised-button color="primary" size="large" class="join-btn" (click)="addToCart()">
            <mat-icon>add_shopping_cart</mat-icon> Add Membership to Cart
          </button>
          
          <button mat-button (click)="goBack()">Back to Menu</button>
        }
      </div>
    </div>
  `,
  styles: [`
    .error { color: red; font-weight: bold; margin-bottom: 20px; }
    .vip-container {
      max-width: 800px;
      margin: 0 auto;
      padding: 40px 20px;
      font-family: 'Segoe UI', sans-serif;
    }
    .vip-header {
      text-align: center;
      margin-bottom: 50px;
    }
    .vip-header h1 {
      font-size: 3rem;
      color: #333;
      margin-bottom: 10px;
    }
    .vip-header p {
      font-size: 1.5rem;
      color: #666;
    }
    .benefits-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 20px;
      margin-bottom: 50px;
    }
    .benefit-card {
      height: 100%;
    }
    .action-section {
      text-align: center;
      background: #f8f9fa;
      padding: 40px;
      border-radius: 16px;
    }
    .price-tag {
      margin-bottom: 30px;
    }
    .price-tag .amount {
      font-size: 3rem;
      font-weight: bold;
      color: #1976d2;
      display: block;
    }
    .join-btn {
      padding: 10px 30px;
      font-size: 1.2rem;
      margin-bottom: 20px;
      display: block;
      margin-left: auto;
      margin-right: auto;
    }
  `]
})
export class VipPageComponent implements OnInit {
  @Input() slug!: string;
  
  private cartService = inject(CartService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private restaurantService = inject(RestaurantService);
  private dialog = inject(MatDialog);

  loading = signal<boolean>(true);
  error = signal<string | null>(null);

  // Define the VIP Membership as a special MenuItem
  // In a real app, this should probably come from the backend
  vipItem: MenuItem = {
    id: -999, // Special ID for VIP Membership
    restaurantId: 0, // Will be updated
    name: 'VIP Membership',
    description: 'Annual VIP Membership with exclusive benefits',
    price: 50.00,
    isAvailable: true,
    imageUrl: 'https://images.unsplash.com/photo-1568602471122-7832951cc4c5?ixlib=rb-4.0.3&auto=format&fit=crop&w=1470&q=80'
  };

  ngOnInit() {
    // We need to get the restaurant ID to set it on the item
    // Assuming the cart service already has context or we fetch it
    if (this.cartService.restaurantId()) {
      this.vipItem.restaurantId = this.cartService.restaurantId()!;
      this.loadVipConfig(this.vipItem.restaurantId);
    } else {
      // Fallback: fetch restaurant by slug if needed, or rely on cart context being set from menu
      this.restaurantService.getFullMenu(this.slug).subscribe(data => {
        this.vipItem.restaurantId = data.restaurant.id;
        this.loadVipConfig(data.restaurant.id);
      });
    }
  }

  loadVipConfig(restaurantId: number) {
    this.loading.set(true);
    this.restaurantService.getVipConfig(restaurantId).subscribe({
      next: (config) => {
        if (config.isEnabled) {
          this.vipItem.price = config.price;
          if (config.description) this.vipItem.description = config.description;
          if (config.imageUrl) this.vipItem.imageUrl = config.imageUrl;
          this.loading.set(false);
        } else {
          this.error.set('VIP Membership is not available at this restaurant.');
          this.loading.set(false);
        }
      },
      error: (err) => {
        console.error('Failed to load VIP config', err);
        this.error.set('Failed to load membership details.');
        this.loading.set(false);
      }
    });
  }

  addToCart() {
    // Check if customer is logged in (using cart service context which holds customer info)
    if (!this.cartService.customerInfo()) {
      this.openMemberLogin();
      return;
    }
    
    this.cartService.addToCart(this.vipItem);
    this.goBack();
  }

  openMemberLogin() {
    const restaurantId = this.vipItem.restaurantId;
    if (!restaurantId) return;

    const dialogRef = this.dialog.open(MemberLoginDialogComponent, {
      width: '400px'
    });
    
    dialogRef.componentInstance.restaurantId = restaurantId;

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.cartService.setCustomer(result.id, result.phoneNumber, result.isMember, result.totalRewardPoints);
        // After successful login, add to cart
        this.cartService.addToCart(this.vipItem);
        this.goBack();
      }
    });
  }

  goBack() {
    const table = this.route.snapshot.queryParamMap.get('table');
    if (table) {
      this.router.navigate([this.slug, 'table', table]);
    } else {
      this.router.navigate([this.slug]);
    }
  }
}
