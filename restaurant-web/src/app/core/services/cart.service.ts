import { computed, Injectable, signal } from '@angular/core';
import { MenuItem } from '../models/restaurant.types';

export interface CartItem {
  menuItem: MenuItem;
  quantity: number;
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  // Signals
  private cartItems = signal<CartItem[]>([]);
  restaurantId = signal<number | null>(null);
  restaurantName = signal<string | null>(null);
  tableId = signal<number | null>(null);

  // Computed values
  readonly items = this.cartItems.asReadonly();
  
  readonly totalCount = computed(() => 
    this.cartItems().reduce((acc, item) => acc + item.quantity, 0)
  );

  readonly totalAmount = computed(() => 
    this.cartItems().reduce((acc, item) => acc + (item.menuItem.price * item.quantity), 0)
  );

  addToCart(menuItem: MenuItem, quantity: number = 1) {
    this.cartItems.update(items => {
      const existingItem = items.find(i => i.menuItem.id === menuItem.id);
      if (existingItem) {
        return items.map(i => 
          i.menuItem.id === menuItem.id 
            ? { ...i, quantity: i.quantity + quantity }
            : i
        );
      }
      return [...items, { menuItem, quantity }];
    });
  }

  removeFromCart(menuItemId: number) {
    this.cartItems.update(items => items.filter(i => i.menuItem.id !== menuItemId));
  }

  updateQuantity(menuItemId: number, quantity: number) {
    if (quantity <= 0) {
      this.removeFromCart(menuItemId);
      return;
    }
    
    this.cartItems.update(items => 
      items.map(i => 
        i.menuItem.id === menuItemId 
          ? { ...i, quantity }
          : i
      )
    );
  }

  clearCart() {
    this.cartItems.set([]);
  }

  setContext(restaurantId: number, restaurantName: string, tableId?: number | null) {
    this.restaurantId.set(restaurantId);
    this.restaurantName.set(restaurantName);
    if (tableId !== undefined) {
      this.tableId.set(tableId);
    }
  }
}
