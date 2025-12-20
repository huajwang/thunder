import { Component, inject, signal } from '@angular/core';
import { RouterModule, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { CartService } from './core/services/cart.service';
import { CartDialogComponent } from './features/cart-dialog/cart-dialog.component';
import { MemberLoginDialogComponent } from './features/customer/member-login-dialog.component';

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

  openCart() {
    const restaurantId = this.cartService.restaurantId();
    if (!restaurantId) return; // Can't open cart if no restaurant context

    this.dialog.open(CartDialogComponent, {
      width: '90%',
      maxWidth: '500px',
      data: {
        restaurantId: restaurantId,
        tableId: this.cartService.tableId()
      }
    });
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
        this.cartService.setCustomer(result.customerId, result.phoneNumber, result.isMember);
      }
    });
  }
}
