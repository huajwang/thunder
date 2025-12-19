import { Component, inject, Input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { RouterModule } from '@angular/router';
import { RestaurantService } from '../../core/services/restaurant.service';
import { CartService } from '../../core/services/cart.service';
import { Category, MenuItem, Restaurant } from '../../core/models/restaurant.types';

@Component({
  selector: 'app-restaurant-menu',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatBadgeModule, RouterModule],
  templateUrl: './restaurant-menu.component.html',
  styleUrl: './restaurant-menu.component.css'
})
export class RestaurantMenuComponent implements OnInit {
  @Input() slug!: string; // From Router
  @Input() tableNumber?: string; // From Router (optional)

  private restaurantService = inject(RestaurantService);
  cartService = inject(CartService);

  restaurant = signal<Restaurant | null>(null);
  categories = signal<Category[]>([]);
  selectedCategory = signal<Category | null>(null);
  loading = signal<boolean>(true);
  error = signal<string | null>(null);
  currentTable = signal<number | null>(null);
  isVipEnabled = signal<boolean>(false);

  ngOnInit() {
    if (this.tableNumber) {
      this.currentTable.set(parseInt(this.tableNumber, 10));
    }
    this.loadData(this.slug);
  }

  loadData(slug: string) {
    this.loading.set(true);
    this.restaurantService.getFullMenu(slug).subscribe({
      next: (data) => {
        this.restaurant.set(data.restaurant);
        this.categories.set(data.categories);
        if (data.categories.length > 0) {
          this.selectedCategory.set(data.categories[0]);
        }
        
        // Set context for CartService
        this.cartService.setContext(data.restaurant.id, data.restaurant.name, this.currentTable());

        // Check VIP status
        this.restaurantService.getVipConfig(data.restaurant.id).subscribe({
          next: (config) => {
            this.isVipEnabled.set(config.isEnabled);
            this.loading.set(false);
          },
          error: () => {
            this.isVipEnabled.set(false);
            this.loading.set(false);
          }
        });
      },
      error: (err) => {
        console.error('Error loading menu:', err);
        this.error.set('Failed to load menu. Please try again.');
        this.loading.set(false);
      }
    });
  }

  addToOrder(item: MenuItem) {
    this.cartService.addToCart(item);
  }

  selectCategory(category: Category) {
    this.selectedCategory.set(category);
  }
}


