import { Component, inject, Input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RestaurantService } from '../../core/services/restaurant.service';
import { Category, MenuItem, Restaurant } from '../../core/models/restaurant.types';

@Component({
  selector: 'app-restaurant-menu',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './restaurant-menu.component.html',
  styleUrl: './restaurant-menu.component.css'
})
export class RestaurantMenuComponent implements OnInit {
  @Input() slug!: string; // From Router

  private restaurantService = inject(RestaurantService);

  restaurant = signal<Restaurant | null>(null);
  categories = signal<Category[]>([]);
  loading = signal<boolean>(true);
  error = signal<string | null>(null);

  ngOnInit() {
    this.loadData(this.slug);
  }

  loadData(slug: string) {
    this.loading.set(true);
    this.restaurantService.getFullMenu(slug).subscribe({
      next: (data) => {
        this.restaurant.set(data.restaurant);
        this.categories.set(data.categories);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading menu:', err);
        this.error.set('Failed to load menu. Please try again.');
        this.loading.set(false);
      }
    });
  }

  addToOrder(item: MenuItem) {
    console.log('Added to order:', item.name);
    // TODO: Implement Cart Logic
  }
}

