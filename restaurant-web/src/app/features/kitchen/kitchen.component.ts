import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { RestaurantService } from '../../core/services/restaurant.service';
import { OrderDetails } from '../../core/models/restaurant.types';

@Component({
  selector: 'app-kitchen',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatChipsModule],
  template: `
    <div class="kitchen-container">
      <header class="kitchen-header">
        <h1>Kitchen Display System</h1>
        <button mat-raised-button color="primary" (click)="refreshOrders()">
          <mat-icon>refresh</mat-icon> Refresh
        </button>
      </header>

      <div class="orders-board">
        <!-- Pending Column -->
        <div class="column pending">
          <h2>Pending</h2>
          @for (order of pendingOrders(); track order.id) {
            <mat-card class="order-card">
              <mat-card-header>
                <mat-card-title>Table #{{ order.tableId }}</mat-card-title>
                <mat-card-subtitle>Order #{{ order.id }} - {{ order.createdAt | date:'shortTime' }}</mat-card-subtitle>
              </mat-card-header>
              <mat-card-content>
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
                <button mat-flat-button color="accent" (click)="updateStatus(order.id, 'PREPARING')">
                  Start Preparing
                </button>
              </mat-card-actions>
            </mat-card>
          }
        </div>

        <!-- Preparing Column -->
        <div class="column preparing">
          <h2>Preparing</h2>
          @for (order of preparingOrders(); track order.id) {
            <mat-card class="order-card preparing-card">
              <mat-card-header>
                <mat-card-title>Table #{{ order.tableId }}</mat-card-title>
                <mat-card-subtitle>Order #{{ order.id }} - {{ order.createdAt | date:'shortTime' }}</mat-card-subtitle>
              </mat-card-header>
              <mat-card-content>
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
                <button mat-flat-button color="primary" (click)="updateStatus(order.id, 'READY')">
                  Mark Ready
                </button>
              </mat-card-actions>
            </mat-card>
          }
        </div>
      </div>
    </div>
  `,
  styles: [`
    .kitchen-container {
      padding: 20px;
      height: 100vh;
      box-sizing: border-box;
      background-color: #f5f5f5;
    }
    .kitchen-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }
    .orders-board {
      display: flex;
      gap: 20px;
      height: calc(100% - 80px);
      overflow-x: auto;
    }
    .column {
      flex: 1;
      min-width: 300px;
      background: #e0e0e0;
      padding: 15px;
      border-radius: 8px;
      overflow-y: auto;
    }
    .column h2 {
      text-align: center;
      margin-top: 0;
      padding-bottom: 10px;
      border-bottom: 2px solid #ccc;
    }
    .order-card {
      margin-bottom: 15px;
    }
    .order-items {
      list-style: none;
      padding: 0;
      margin: 10px 0;
    }
    .order-items li {
      display: flex;
      gap: 10px;
      margin-bottom: 5px;
      font-size: 1.1rem;
    }
    .qty {
      font-weight: bold;
      color: #d32f2f;
    }
    .preparing-card {
      border-left: 5px solid #ff9800;
    }
  `]
})
export class KitchenComponent implements OnInit {
  private restaurantService = inject(RestaurantService);
  private route = inject(ActivatedRoute);
  
  restaurantId: number | null = null;

  pendingOrders = signal<OrderDetails[]>([]);
  preparingOrders = signal<OrderDetails[]>([]);

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
    this.restaurantService.getOrders(this.restaurantId, ['PENDING', 'PREPARING']).subscribe(orders => {
      this.pendingOrders.set(orders.filter(o => o.status === 'PENDING'));
      this.preparingOrders.set(orders.filter(o => o.status === 'PREPARING'));
    });
  }

  updateStatus(orderId: number, status: string) {
    this.restaurantService.updateOrderStatus(orderId, status).subscribe(() => {
      this.refreshOrders();
    });
  }
}
