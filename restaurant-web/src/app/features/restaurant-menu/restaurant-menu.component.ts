import { Component, inject, Input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { RestaurantService } from '../../core/services/restaurant.service';
import { CartService } from '../../core/services/cart.service';
import { Category, MenuItem, Restaurant } from '../../core/models/restaurant.types';
import { CartDialogComponent } from '../cart-dialog/cart-dialog.component';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

@Component({
  selector: 'app-restaurant-menu',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatBadgeModule, MatDialogModule],
  templateUrl: './restaurant-menu.component.html',
  styleUrl: './restaurant-menu.component.css'
})
export class RestaurantMenuComponent implements OnInit {
  @Input() slug!: string; // From Router
  @Input() tableNumber?: string; // From Router (optional)

  private restaurantService = inject(RestaurantService);
  cartService = inject(CartService);
  private dialog = inject(MatDialog);

  restaurant = signal<Restaurant | null>(null);
  categories = signal<Category[]>([]);
  loading = signal<boolean>(true);
  error = signal<string | null>(null);
  currentTable = signal<number | null>(null);

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
        this.loading.set(false);
        
        // Set context for CartService
        this.cartService.setContext(data.restaurant.id, data.restaurant.name, this.currentTable());
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
}


