import { computed, Injectable, signal } from '@angular/core';
import { MenuItem } from '../models/restaurant.types';
import { TAX_RATE } from '../constants';

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
  restaurantSlug = signal<string | null>(null);
  restaurantName = signal<string | null>(null);
  tableId = signal<number | null>(null);
  customerId = signal<number | null>(null);
  customerInfo = signal<{phoneNumber: string, isMember: boolean} | null>(null);
  vipDiscountRate = signal<number>(0);

  // Computed values
  readonly items = this.cartItems.asReadonly();
  
  readonly totalCount = computed(() => 
    this.cartItems().reduce((acc, item) => acc + item.quantity, 0)
  );

  readonly totalAmount = computed(() => 
    this.cartItems().reduce((acc, item) => acc + (item.menuItem.price * item.quantity), 0)
  );

  readonly taxRate = TAX_RATE;

  readonly discountAmount = computed(() => {
    const items = this.cartItems();
    const hasVipItem = items.some(item => item.menuItem.id === -999 || item.menuItem.name === 'VIP Membership');
    
    if (this.customerInfo()?.isMember || hasVipItem) {
      // Calculate discountable amount (exclude VIP membership item)
      const discountableAmount = items.reduce((acc, item) => {
        if (item.menuItem.id === -999 || item.menuItem.name === 'VIP Membership') {
          return acc;
        }
        return acc + (item.menuItem.price * item.quantity);
      }, 0);
      
      return discountableAmount * this.vipDiscountRate();
    }
    return 0;
  });

  readonly discountedSubTotal = computed(() => 
    this.totalAmount() - this.discountAmount()
  );

  readonly taxAmount = computed(() => 
    this.discountedSubTotal() * this.taxRate
  );

  readonly finalTotal = computed(() => 
    this.discountedSubTotal() + this.taxAmount()
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

  setContext(restaurantId: number, slug: string, restaurantName: string, tableId?: number | null, customerId?: number | null) {
    this.restaurantId.set(restaurantId);
    this.restaurantSlug.set(slug);
    this.restaurantName.set(restaurantName);
    if (tableId !== undefined) {
      this.tableId.set(tableId);
    }
    if (customerId !== undefined) {
      this.customerId.set(customerId);
    }
  }

  setCustomer(id: number, phoneNumber: string, isMember: boolean) {
    this.customerId.set(id);
    this.customerInfo.set({ phoneNumber, isMember });
    // Persist to localStorage
    localStorage.setItem('customer_info', JSON.stringify({ id, phoneNumber, isMember }));
  }

  restoreCustomer() {
    const stored = localStorage.getItem('customer_info');
    if (stored) {
      const { id, phoneNumber, isMember } = JSON.parse(stored);
      this.setCustomer(id, phoneNumber, isMember);
    }
  }
}
