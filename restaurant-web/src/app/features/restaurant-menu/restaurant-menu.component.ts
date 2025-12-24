import { Component, inject, Input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { Router, RouterModule } from '@angular/router';
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
  private router = inject(Router);
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

  getSpicinessArray(level: number): number[] {
    return Array(level).fill(0);
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
        this.cartService.setContext(data.restaurant.id, data.restaurant.slug, data.restaurant.name, this.currentTable());

        // Set AYCE config
        if (data.restaurant.type === 'AYCE') {
          this.cartService.setAyceConfig('AYCE');
        } else {
          this.cartService.setAyceConfig('STANDARD');
        }

        // Check VIP status
        this.restaurantService.getVipConfig(data.restaurant.id).subscribe({
          next: (config) => {
            this.isVipEnabled.set(config.isEnabled);
            this.cartService.vipDiscountRate.set(config.discountRate || 0);
            this.loading.set(false);
          },
          error: () => {
            this.isVipEnabled.set(false);
            this.cartService.vipDiscountRate.set(0);
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
    if (item.variants && item.variants.length > 0) {
      this.router.navigate(['/', this.slug, 'dish', item.id], { 
        queryParams: this.currentTable() ? { table: this.currentTable() } : {} 
      });
    } else {
      this.cartService.addToCart(item);
    }
  }

  selectCategory(category: Category) {
    this.selectedCategory.set(category);
  }
}


