import { Component, inject, Input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatRadioModule } from '@angular/material/radio';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { RestaurantService } from '../../core/services/restaurant.service';
import { CartService } from '../../core/services/cart.service';
import { MenuItem, MenuItemVariant } from '../../core/models/restaurant.types';

@Component({
  selector: 'app-dish-detail',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatRadioModule, FormsModule, RouterModule],
  templateUrl: './dish-detail.component.html',
  styleUrl: './dish-detail.component.css'
})
export class DishDetailComponent implements OnInit {
  @Input() slug!: string;
  @Input() dishId!: string;
  @Input() table?: string;

  private restaurantService = inject(RestaurantService);
  private cartService = inject(CartService);
  private router = inject(Router);

  item = signal<MenuItem | null>(null);
  selectedVariant = signal<MenuItemVariant | null>(null);
  loading = signal<boolean>(true);
  error = signal<string | null>(null);

  ngOnInit() {
    this.loadData();
  }

  getSpicinessArray(level: number): number[] {
    return Array(level).fill(0);
  }

  loadData() {
    this.loading.set(true);
    this.restaurantService.getMenuItem(parseInt(this.dishId, 10)).subscribe({
      next: (data) => {
        this.item.set(data);
        if (data.variants && data.variants.length > 0) {
          this.selectedVariant.set(data.variants[0]);
        }
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Failed to load dish details');
        this.loading.set(false);
      }
    });
  }

  addToCart() {
    const currentItem = this.item();
    if (!currentItem) return;

    const variant = this.selectedVariant();
    
    this.cartService.addToCart(currentItem, variant || undefined);
    
    this.goBack();
  }
  
  goBack() {
    if (this.table) {
      this.router.navigate(['/', this.slug, 'table', this.table]);
    } else {
      this.router.navigate(['/', this.slug]);
    }
  }
}
