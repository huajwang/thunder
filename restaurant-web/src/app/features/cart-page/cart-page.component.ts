import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { RestaurantService } from '../../core/services/restaurant.service';
import { StandardCartComponent } from './standard-cart/standard-cart.component';
import { AyceCartComponent } from './ayce-cart/ayce-cart.component';

@Component({
  selector: 'app-cart-page',
  standalone: true,
  imports: [
    CommonModule,
    StandardCartComponent,
    AyceCartComponent
  ],
  templateUrl: './cart-page.component.html',
  styleUrl: './cart-page.component.css'
})
export class CartPageComponent implements OnInit {
  cartService = inject(CartService);
  private restaurantService = inject(RestaurantService);
  private route = inject(ActivatedRoute);

  ngOnInit() {
    // Try to restore customer info if missing
    if (!this.cartService.customerInfo()) {
      this.cartService.restoreCustomer();
    }

    const slug = this.route.snapshot.paramMap.get('slug');
    if (slug) {
      this.loadRestaurantData(slug);
    }
  }

  private loadRestaurantData(slug: string) {
    this.restaurantService.getRestaurantBySlug(slug).subscribe({
      next: (restaurant) => {
        // Ensure CartService has the correct context
        if (!this.cartService.restaurantId()) {
          this.cartService.setContext(restaurant.id, restaurant.slug, restaurant.name);
        }

        // Fetch VIP config to ensure discount rate is set
        this.restaurantService.getVipConfig(restaurant.id).subscribe({
          next: (config) => {
            this.cartService.vipDiscountRate.set(config.discountRate || 0);
          },
          error: () => {
            this.cartService.vipDiscountRate.set(0);
          }
        });
      },
      error: (err) => console.error('Error loading restaurant:', err)
    });
  }
}
