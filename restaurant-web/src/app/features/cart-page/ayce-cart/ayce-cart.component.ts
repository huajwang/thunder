import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { CartService } from '../../../core/services/cart.service';
import { RestaurantService } from '../../../core/services/restaurant.service';
import { OrderRequest } from '../../../core/models/restaurant.types';

@Component({
  selector: 'app-ayce-cart',
  standalone: true,
  imports: [
    CommonModule, 
    MatButtonModule, 
    MatIconModule, 
    MatListModule
  ],
  templateUrl: './ayce-cart.component.html',
  styleUrl: '../cart-page.component.css'
})
export class AyceCartComponent {
  cartService = inject(CartService);
  private restaurantService = inject(RestaurantService);
  private router = inject(Router);

  isSubmitting = false;
  error: string | null = null;

  increaseQuantity(itemId: number, currentQty: number, variantId?: number) {
    this.cartService.updateQuantity(itemId, currentQty + 1, variantId);
  }

  decreaseQuantity(itemId: number, currentQty: number, variantId?: number) {
    this.cartService.updateQuantity(itemId, currentQty - 1, variantId);
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
        quantity: item.quantity,
        variantId: item.variant?.id
      }))
    };

    this.restaurantService.placeOrder(orderRequest).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        
        // Update customer points if logged in
        const currentCustomer = this.cartService.customerInfo();
        const currentCustomerId = this.cartService.customerId();
        
        if (currentCustomer && currentCustomerId && response.totalRewardPoints !== undefined) {
          this.cartService.setCustomer(
            currentCustomerId,
            currentCustomer.phoneNumber,
            currentCustomer.isMember,
            response.totalRewardPoints
          );
        }

        // Add to placed orders
        this.cartService.addPlacedOrder({
          items: [...this.cartService.items()],
          total: this.cartService.finalTotal(),
          date: new Date()
        });

        this.cartService.clearCart();
      },
      error: (err) => {
        console.error('Order failed', err);
        this.isSubmitting = false;
        this.error = 'Failed to place order. Please try again.';
      }
    });
  }
}
