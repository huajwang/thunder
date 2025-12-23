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

  readonly hasVipItem = computed(() => 
    this.cartItems().some(item => item.menuItem.id === -999 || item.menuItem.name === 'VIP Membership')
  );

  readonly taxRate = TAX_RATE;

  readonly discountAmount = computed(() => {
    const items = this.cartItems();
    
    if (this.customerInfo()?.isMember || this.hasVipItem()) {
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

  constructor() {
    // Removed automatic restore in constructor to wait for restaurant context
  }

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
    const itemToRemove = this.cartItems().find(i => i.menuItem.id === menuItemId);
    this.cartItems.update(items => items.filter(i => i.menuItem.id !== menuItemId));

    // If removing VIP membership, ensure local member status is reset to false
    if (itemToRemove && (itemToRemove.menuItem.id === -999 || itemToRemove.menuItem.name === 'VIP Membership')) {
      const currentInfo = this.customerInfo();
      const currentId = this.customerId();
      if (currentInfo && currentId) {
        this.setCustomer(currentId, currentInfo.phoneNumber, false);
      }
    }
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
    // If switching restaurants, clear previous state
    if (this.restaurantId() !== restaurantId) {
      this.clearCart();
      this.customerId.set(null);
      this.customerInfo.set(null);
    }

    this.restaurantId.set(restaurantId);
    this.restaurantSlug.set(slug);
    this.restaurantName.set(restaurantName);
    if (tableId !== undefined) {
      this.tableId.set(tableId);
    }
    if (customerId !== undefined) {
      this.customerId.set(customerId);
    }
    
    this.restoreCustomer();
  }

  setCustomer(id: number, phoneNumber: string, isMember: boolean) {
    this.customerId.set(id);
    this.customerInfo.set({ phoneNumber, isMember });
    
    const rid = this.restaurantId();
    if (rid) {
      localStorage.setItem(`customer_info_${rid}`, JSON.stringify({ id, phoneNumber, isMember }));
    }
  }

  restoreCustomer() {
    const rid = this.restaurantId();
    if (!rid) return;

    const stored = localStorage.getItem(`customer_info_${rid}`);
    if (stored) {
      const { id, phoneNumber, isMember } = JSON.parse(stored);
      this.customerId.set(id);
      this.customerInfo.set({ phoneNumber, isMember });
    }
  }
}
