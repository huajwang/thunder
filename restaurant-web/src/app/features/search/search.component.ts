import { Component, inject, Input, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { Router, RouterModule } from '@angular/router';
import { Subject, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError, takeUntil } from 'rxjs/operators';
import { RestaurantService } from '../../core/services/restaurant.service';
import { CartService } from '../../core/services/cart.service';
import { MenuItem, Restaurant } from '../../core/models/restaurant.types';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    RouterModule
  ],
  template: `
    <div class="container">
      <div class="search-header">
        <button mat-icon-button (click)="goBack()">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <mat-form-field appearance="outline" class="search-field">
          <mat-icon matPrefix>search</mat-icon>
          <input matInput [(ngModel)]="searchQuery" (ngModelChange)="onSearchInput($event)" placeholder="Search menu items..." autofocus>
          @if (searchQuery) {
            <button mat-icon-button matSuffix (click)="clearSearch()">
              <mat-icon>close</mat-icon>
            </button>
          }
        </mat-form-field>
      </div>

      <div class="results-container">
        @if (loading()) {
          <div class="loading-spinner">Loading...</div>
        } @else {
          <div class="items-grid">
            @for (item of results(); track item.id) {
              <div class="menu-item-card">
                @if (item.imageUrl) {
                  <img [src]="item.imageUrl" [alt]="item.name" class="item-image">
                }
                <div class="item-details">
                  <h3>{{ item.name }}</h3>
                  <p class="item-desc">{{ item.description }}</p>
                  <span class="item-price">\${{ item.price.toFixed(2) }}</span>
                </div>
                <button mat-mini-fab color="primary" (click)="addToOrder(item)">
                  <mat-icon>add</mat-icon>
                </button>
              </div>
            }
            @if (results().length === 0 && hasSearched()) {
              <div class="no-results">
                <p>No items found matching "{{ lastQuery() }}"</p>
              </div>
            }
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .container {
      max-width: 800px;
      margin: 0 auto;
      padding: 20px;
    }
    .search-header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 20px;
    }
    .search-field {
      flex: 1;
    }
    .items-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;
    }
    .menu-item-card {
      background: white;
      border-radius: 12px;
      overflow: hidden;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      display: flex;
      flex-direction: column;
      position: relative;
    }
    .item-image {
      width: 100%;
      height: 180px;
      object-fit: cover;
    }
    .item-details {
      padding: 15px;
      flex: 1;
    }
    .item-details h3 {
      margin: 0 0 8px 0;
      font-size: 1.1rem;
    }
    .item-desc {
      color: #666;
      font-size: 0.9rem;
      margin: 0 0 10px 0;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    .item-price {
      font-weight: 600;
      color: #1976d2;
      font-size: 1.1rem;
    }
    .menu-item-card button {
      position: absolute;
      bottom: 15px;
      right: 15px;
    }
    .no-results {
      text-align: center;
      padding: 40px;
      color: #666;
      grid-column: 1 / -1;
    }
    .loading-spinner {
      text-align: center;
      padding: 20px;
      color: #666;
    }
  `]
})
export class SearchComponent implements OnInit, OnDestroy {
  @Input() slug!: string;
  
  private restaurantService = inject(RestaurantService);
  private cartService = inject(CartService);
  private router = inject(Router);
  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  searchQuery = '';
  results = signal<MenuItem[]>([]);
  loading = signal<boolean>(false);
  hasSearched = signal<boolean>(false);
  lastQuery = signal<string>('');
  restaurantId = signal<number | null>(null);

  ngOnInit() {
    // Get restaurant ID from slug
    this.restaurantService.getRestaurantBySlug(this.slug).subscribe(restaurant => {
      this.restaurantId.set(restaurant.id);
    });

    // Setup live search
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(query => {
        const id = this.restaurantId();
        if (!query.trim() || !id) {
          this.loading.set(false);
          return of([]);
        }
        
        this.loading.set(true);
        this.hasSearched.set(true);
        this.lastQuery.set(query);
        
        return this.restaurantService.searchMenuItems(id, query).pipe(
          catchError(() => {
            this.loading.set(false);
            return of([]);
          })
        );
      }),
      takeUntil(this.destroy$)
    ).subscribe(items => {
      this.results.set(items);
      this.loading.set(false);
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSearchInput(query: string) {
    this.searchSubject.next(query);
  }

  clearSearch() {
    this.searchQuery = '';
    this.results.set([]);
    this.hasSearched.set(false);
    this.searchSubject.next('');
  }

  addToOrder(item: MenuItem) {
    this.cartService.addToCart(item);
  }

  goBack() {
    const tableId = this.cartService.tableId();
    if (tableId) {
      this.router.navigate(['/', this.slug, 'table', tableId]);
    } else {
      this.router.navigate(['/', this.slug]);
    }
  }
}
