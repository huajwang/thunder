import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { CartService } from '../../../core/services/cart.service';
import { RestaurantService } from '../../../core/services/restaurant.service';
import { AddressService, AddressSuggestion } from '../../../core/services/address.service';
import { OrderRequest } from '../../../core/models/restaurant.types';

@Component({
  selector: 'app-standard-cart',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule,
    MatButtonModule, 
    MatIconModule, 
    MatListModule,
    MatProgressSpinnerModule,
    MatRadioModule,
    MatInputModule,
    MatFormFieldModule,
    MatTooltipModule
  ],
  templateUrl: './standard-cart.component.html',
  styleUrl: '../cart-page.component.css'
})
export class StandardCartComponent implements OnInit {
  cartService = inject(CartService);
  private restaurantService = inject(RestaurantService);
  private addressService = inject(AddressService);
  private router = inject(Router);

  isSubmitting = false;
  error: string | null = null;
  
  orderType = signal<'dine-in' | 'delivery'>('dine-in');
  deliveryAddress = signal<string>('');
  phoneNumber = signal<string>('');
  
  // Address Search
  postalCode = signal<string>('');
  addressQuery = signal<string>('');
  addressSuggestions = signal<AddressSuggestion[]>([]);
  isSearchingAddress = signal<boolean>(false);
  isLocating = signal<boolean>(false);
  
  private searchSubject = new Subject<{ query: string, postalCode: string }>();

  constructor() {
    // If no table ID is present, default to delivery
    if (!this.cartService.tableId()) {
      this.orderType.set('delivery');
    }

    // Setup search debounce
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged((prev, curr) => prev.query === curr.query && prev.postalCode === curr.postalCode),
      switchMap(searchParams => {
        this.isSearchingAddress.set(true);
        return this.addressService.searchAddress(searchParams.query, searchParams.postalCode);
      })
    ).subscribe(suggestions => {
      this.addressSuggestions.set(suggestions);
      this.isSearchingAddress.set(false);
    });
  }

  ngOnInit() {
    // Try to restore customer info if missing
    if (!this.cartService.customerInfo()) {
      this.cartService.restoreCustomer();
    }
  }
  
  onAddressSearch(query: string) {
    this.addressQuery.set(query);
    this.triggerSearch();
  }

  onPostalCodeChange(code: string) {
    this.postalCode.set(code);
    this.triggerSearch();
  }

  onPhoneNumberChange(phone: string) {
    this.phoneNumber.set(phone);
    if (this.error === 'Please enter a phone number.' && phone) {
      this.error = null;
    }
  }

  private triggerSearch() {
    const query = this.addressQuery();
    const postal = this.postalCode();
    
    if ((query && query.length > 2) || (postal && postal.length >= 3)) {
      this.searchSubject.next({ query, postalCode: postal });
    } else {
      this.addressSuggestions.set([]);
    }
  }

  locateMe() {
    if (!navigator.geolocation) {
      this.error = 'Geolocation is not supported by your browser';
      return;
    }

    this.isLocating.set(true);
    this.error = null;

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lat = position.coords.latitude;
        const lon = position.coords.longitude;

        this.addressService.reverseGeocode(lat, lon).subscribe({
          next: (suggestion) => {
            this.isLocating.set(false);
            if (suggestion) {
              this.deliveryAddress.set(suggestion.display_name);
              
              // Try to extract postal code if available
              if (suggestion.address && suggestion.address.postcode) {
                this.postalCode.set(suggestion.address.postcode);
              }
            } else {
              this.error = 'Could not determine address from your location';
            }
          },
          error: () => {
            this.isLocating.set(false);
            this.error = 'Error getting address details';
          }
        });
      },
      (err) => {
        this.isLocating.set(false);
        this.error = 'Unable to retrieve your location';
        console.error(err);
      }
    );
  }

  selectAddress(suggestion: AddressSuggestion) {
    this.deliveryAddress.set(suggestion.display_name);
    this.addressQuery.set('');
    this.addressSuggestions.set([]);
  }

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

    if (this.orderType() === 'delivery') {
      if (!this.deliveryAddress()) {
        this.error = 'Please enter a delivery address.';
        return;
      }
      if (!this.phoneNumber()) {
        this.error = 'Please enter a phone number.';
        return;
      }
    }

    this.isSubmitting = true;
    this.error = null;

    const orderRequest: OrderRequest = {
      restaurantId: restaurantId,
      tableId: this.orderType() === 'dine-in' ? (this.cartService.tableId() || undefined) : undefined,
      customerId: this.cartService.customerId() || undefined,
      deliveryAddress: this.orderType() === 'delivery' ? this.deliveryAddress() : undefined,
      phoneNumber: this.orderType() === 'delivery' ? this.phoneNumber() : undefined,
      items: this.cartService.items().map(item => ({
        menuItemId: item.menuItem.id,
        quantity: item.quantity,
        variantId: item.variant?.id
      }))
    };

    // Check if we are buying a membership
    const hasVipItem = this.cartService.items().some(item => item.menuItem.id === -999 || item.menuItem.name === 'VIP Membership');
    const currentCustomer = this.cartService.customerInfo();
    const currentCustomerId = this.cartService.customerId();

    this.restaurantService.placeOrder(orderRequest).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        
        // If we bought a membership, upgrade the local customer state
        if (hasVipItem && currentCustomer && currentCustomerId) {
          this.cartService.setCustomer(currentCustomerId, currentCustomer.phoneNumber, true);
        }

        this.cartService.clearCart();
        alert(`Order placed successfully! Order ID: ${response.id}`);
        this.goBack();
      },
      error: (err) => {
        console.error('Order failed', err);
        this.isSubmitting = false;
        this.error = 'Failed to place order. Please try again.';
      }
    });
  }
}
