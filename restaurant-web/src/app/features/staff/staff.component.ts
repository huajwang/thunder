import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RestaurantService } from '../../core/services/restaurant.service';
import { OrderDetails } from '../../core/models/restaurant.types';

@Component({
  selector: 'app-staff',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule],
  template: `
    <div class="staff-container">
      <header class="staff-header">
        <h1>Service Station</h1>
        <button mat-raised-button color="primary" (click)="refreshOrders()">
          <mat-icon>refresh</mat-icon> Refresh
        </button>
      </header>

      <div class="orders-grid">
        @if (readyOrders().length === 0) {
          <div class="no-orders">
            <mat-icon>check_circle</mat-icon>
            <p>All caught up! No orders ready to serve.</p>
          </div>
        }

        @for (order of readyOrders(); track order.id) {
          <mat-card class="order-card ready-card">
            <mat-card-header>
              <div mat-card-avatar class="table-avatar">{{ order.tableId }}</div>
              <mat-card-title>Table #{{ order.tableId }}</mat-card-title>
              <mat-card-subtitle>Order #{{ order.id }}</mat-card-subtitle>
            </mat-card-header>
            <mat-card-content>
              <div class="time-elapsed">
                Ready since: {{ order.updatedAt || order.createdAt | date:'shortTime' }}
              </div>
              <ul class="order-items">
                @for (item of order.items; track item.menuItemId) {
                  <li>
                    <span class="qty">{{ item.quantity }}x</span>
                    <span class="name">{{ item.menuItemName }}</span>
                  </li>
                }
              </ul>
            </mat-card-content>
            <mat-card-actions align="end">
              <button mat-raised-button color="primary" (click)="markServed(order.id)">
                <mat-icon>room_service</mat-icon> Serve
              </button>
            </mat-card-actions>
          </mat-card>
        }
      </div>
    </div>
  `,
  styles: [`
    .staff-container {
      padding: 20px;
      background-color: #f5f5f5;
      min-height: 100vh;
    }
    .staff-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }
    .orders-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;
    }
    .order-card {
      border-left: 6px solid #4caf50;
    }
    .table-avatar {
      background-color: #1976d2;
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
      font-size: 1.2rem;
      border-radius: 50%;
      width: 40px;
      height: 40px;
    }
    .order-items {
      list-style: none;
      padding: 0;
      margin: 15px 0;
      border-top: 1px solid #eee;
      padding-top: 10px;
    }
    .order-items li {
      display: flex;
      gap: 10px;
      margin-bottom: 5px;
    }
    .qty {
      font-weight: bold;
    }
    .no-orders {
      grid-column: 1 / -1;
      text-align: center;
      padding: 50px;
      color: #777;
    }
    .no-orders mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      margin-bottom: 10px;
      color: #ccc;
    }
  `]
})
export class StaffComponent implements OnInit {
  private restaurantService = inject(RestaurantService);
  private route = inject(ActivatedRoute);
  
  restaurantId: number | null = null;
  readyOrders = signal<OrderDetails[]>([]);

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const slug = params.get('slug');
      if (slug) {
        this.restaurantService.getRestaurantBySlug(slug).subscribe(restaurant => {
          this.restaurantId = restaurant.id;
          this.refreshOrders();
          this.subscribeToUpdates();
        });
      }
    });
  }

  subscribeToUpdates() {
    if (!this.restaurantId) return;
    this.restaurantService.getOrderStream(this.restaurantId).subscribe({
      next: (event) => {
        console.log('Received order update:', event);
        this.refreshOrders();
      },
      error: (err) => console.error('SSE Error:', err)
    });
  }

  refreshOrders() {
    if (!this.restaurantId) return;
    this.restaurantService.getOrders(this.restaurantId, ['READY']).subscribe(orders => {
      this.readyOrders.set(orders);
    });
  }

  markServed(orderId: number) {
    this.restaurantService.updateOrderStatus(orderId, 'COMPLETED').subscribe(() => {
      this.refreshOrders();
    });
  }
}
