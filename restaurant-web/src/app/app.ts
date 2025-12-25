import { Component, inject, signal } from '@angular/core';
import { RouterModule, RouterOutlet, Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { CartService } from './core/services/cart.service';
import { MemberLoginDialogComponent } from './features/customer/member-login-dialog.component';
import { RewardsDialogComponent } from './features/customer/rewards-dialog.component';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet, 
    RouterModule,
    MatToolbarModule, 
    MatButtonModule, 
    MatIconModule, 
    MatMenuModule,
    MatBadgeModule,
    MatDialogModule
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('restaurant-web');
  cartService = inject(CartService);
  private dialog = inject(MatDialog);
  private router = inject(Router);

  openCart() {
    const restaurantSlug = this.cartService.restaurantSlug();
    if (!restaurantSlug) return; // Can't open cart if no restaurant context

    this.router.navigate(['/', restaurantSlug, 'cart']);
  }

  openMemberLogin() {
    const restaurantId = this.cartService.restaurantId();
    if (!restaurantId) return;

    const dialogRef = this.dialog.open(MemberLoginDialogComponent, {
      width: '400px'
    });
    
    dialogRef.componentInstance.restaurantId = restaurantId;

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.cartService.setCustomer(result.id, result.phoneNumber, result.isMember, result.totalRewardPoints);
      }
    });
  }

  openRewards() {
    const restaurantId = this.cartService.restaurantId();
    const customerId = this.cartService.customerId();
    const customerInfo = this.cartService.customerInfo();
    
    if (!restaurantId || !customerId || !customerInfo) return;

    const dialogRef = this.dialog.open(RewardsDialogComponent, {
      width: '400px',
      maxHeight: '80vh'
    });
    
    dialogRef.componentInstance.restaurantId = restaurantId;
    dialogRef.componentInstance.customerId = customerId;
    dialogRef.componentInstance.totalPoints = customerInfo.totalRewardPoints || 0;
  }
}
