import { Component, inject, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CartService } from '../../core/services/cart.service';
import { RestaurantService } from '../../core/services/restaurant.service';
import { OrderRequest } from '../../core/models/restaurant.types';

@Component({
  selector: 'app-cart-dialog',
  standalone: true,
  imports: [
    CommonModule, 
    MatDialogModule, 
    MatButtonModule, 
    MatIconModule, 
    MatListModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './cart-dialog.component.html',
  styleUrl: './cart-dialog.component.css'
})
export class CartDialogComponent {
  cartService = inject(CartService);
  private restaurantService = inject(RestaurantService);
  private dialogRef = inject(MatDialogRef<CartDialogComponent>);

  isSubmitting = false;
  error: string | null = null;

  constructor(@Inject(MAT_DIALOG_DATA) public data: { restaurantId: number, tableId?: number }) {}

  increaseQuantity(itemId: number, currentQty: number) {
    this.cartService.updateQuantity(itemId, currentQty + 1);
  }

  decreaseQuantity(itemId: number, currentQty: number) {
    this.cartService.updateQuantity(itemId, currentQty - 1);
  }

  placeOrder() {
    if (this.cartService.items().length === 0) return;

    this.isSubmitting = true;
    this.error = null;

    const orderRequest: OrderRequest = {
      restaurantId: this.data.restaurantId,
      tableId: this.data.tableId,
      customerId: this.cartService.customerId() || undefined,
      items: this.cartService.items().map(item => ({
        menuItemId: item.menuItem.id,
        quantity: item.quantity
      }))
    };

    this.restaurantService.placeOrder(orderRequest).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.cartService.clearCart();
        this.dialogRef.close(true); // Close with success
        // Ideally show a success snackbar or navigate to order status
        alert(`Order placed successfully! Order ID: ${response.id}`);
      },
      error: (err) => {
        console.error('Order failed', err);
        this.isSubmitting = false;
        this.error = 'Failed to place order. Please try again.';
      }
    });
  }
}
