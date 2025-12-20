import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CartService } from '../../core/services/cart.service';
import { RestaurantService } from '../../core/services/restaurant.service';
import { OrderRequest } from '../../core/models/restaurant.types';

@Component({
  selector: 'app-cart-page',
  standalone: true,
  imports: [
    CommonModule, 
    MatButtonModule, 
    MatIconModule, 
    MatListModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './cart-page.component.html',
  styleUrl: './cart-page.component.css'
})
export class CartPageComponent {
  cartService = inject(CartService);
  private restaurantService = inject(RestaurantService);
  private router = inject(Router);

  isSubmitting = false;
  error: string | null = null;

  increaseQuantity(itemId: number, currentQty: number) {
    this.cartService.updateQuantity(itemId, currentQty + 1);
  }

  decreaseQuantity(itemId: number, currentQty: number) {
    this.cartService.updateQuantity(itemId, currentQty - 1);
  }

  goBack() {
    const slug = this.cartService.restaurantSlug();
    const tableId = this.cartService.tableId();
    if (slug) {
      if (tableId) {
        this.router.navigate(['/', slug, 'table', tableId]);
      } else {
        this.router.navigate(['/', slug]);
      }
    } else {
      // Fallback if no slug is present (shouldn't happen in normal flow)
      this.router.navigate(['/']);
    }
  }

  placeOrder() {
    if (this.cartService.items().length === 0) return;

    const restaurantId = this.cartService.restaurantId();
    if (!restaurantId) {
      this.error = 'Restaurant information missing.';
      return;
    }

    this.isSubmitting = true;
    this.error = null;

    const orderRequest: OrderRequest = {
      restaurantId: restaurantId,
      tableId: this.cartService.tableId() || undefined,
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
        alert(`Order placed successfully! Order ID: ${response.id}`);
        this.goBack();
      },
      error: (err) => {
        console.error('Order failed', err);
        this.isSubmitting = false;
        this.error = 'Failed to place order. Please try again.';
      }
    });
  }
}
