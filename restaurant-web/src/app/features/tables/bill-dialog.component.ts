import { Component, inject, Inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatListModule } from '@angular/material/list';
import { RestaurantService } from '../../core/services/restaurant.service';
import { OrderDetails } from '../../core/models/restaurant.types';

@Component({
  selector: 'app-bill-dialog',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatDialogModule, MatListModule],
  template: `
    <h2 mat-dialog-title>Bill for Table #{{ data.tableNumber }}</h2>
    <mat-dialog-content>
      @if (loading()) {
        <p>Loading bill...</p>
      } @else if (orders().length === 0) {
        <p>No active orders for this table.</p>
      } @else {
        <div class="bill-content">
          @for (order of orders(); track order.id) {
            <div class="order-section">
              <h3>Order #{{ order.id }} <span class="status-badge">{{ order.status }}</span></h3>
              <ul class="bill-items">
                @for (item of order.items; track item.menuItemId) {
                  <li>
                    <span class="item-name">{{ item.quantity }}x {{ item.menuItemName }}</span>
                    <span class="item-price">\${{ (item.price * item.quantity).toFixed(2) }}</span>
                  </li>
                }
              </ul>
            </div>
          }
          
          <div class="total-section">
            <span>Total Amount:</span>
            <span class="total-price">\${{ totalAmount().toFixed(2) }}</span>
          </div>
        </div>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button 
        mat-raised-button 
        color="primary" 
        [disabled]="orders().length === 0"
        (click)="checkout()">
        <mat-icon>payment</mat-icon> Checkout & Close
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .bill-content {
      padding: 10px 0;
    }
    .order-section {
      margin-bottom: 20px;
      border-bottom: 1px dashed #ccc;
      padding-bottom: 10px;
    }
    .order-section h3 {
      margin: 0 0 10px 0;
      font-size: 1rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .status-badge {
      font-size: 0.8rem;
      background: #eee;
      padding: 2px 8px;
      border-radius: 12px;
      font-weight: normal;
    }
    .bill-items {
      list-style: none;
      padding: 0;
      margin: 0;
    }
    .bill-items li {
      display: flex;
      justify-content: space-between;
      margin-bottom: 5px;
      font-size: 0.95rem;
    }
    .total-section {
      display: flex;
      justify-content: space-between;
      font-size: 1.2rem;
      font-weight: bold;
      margin-top: 20px;
      padding-top: 10px;
      border-top: 2px solid #333;
    }
  `]
})
export class BillDialogComponent implements OnInit {
  private restaurantService = inject(RestaurantService);
  private dialogRef = inject(MatDialogRef<BillDialogComponent>);
  
  orders = signal<OrderDetails[]>([]);
  loading = signal(true);
  totalAmount = signal(0);

  constructor(@Inject(MAT_DIALOG_DATA) public data: { tableId: number, tableNumber: number }) {}

  ngOnInit() {
    this.restaurantService.getTableBill(this.data.tableId).subscribe({
      next: (orders) => {
        this.orders.set(orders);
        const total = orders.reduce((sum, order) => sum + order.totalAmount, 0);
        this.totalAmount.set(total);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  checkout() {
    if (confirm(`Confirm checkout for Table #${this.data.tableNumber}? Total: $${this.totalAmount().toFixed(2)}`)) {
      this.restaurantService.checkoutTable(this.data.tableId).subscribe(() => {
        this.dialogRef.close(true);
      });
    }
  }
}
