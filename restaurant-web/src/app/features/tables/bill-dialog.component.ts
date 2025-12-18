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
            <div class="summary-row">
              <span>Subtotal:</span>
              <span>\${{ subtotal().toFixed(2) }}</span>
            </div>
            @if (discount() > 0) {
              <div class="summary-row discount">
                <span>Member Discount (10%):</span>
                <span>-\${{ discount().toFixed(2) }}</span>
              </div>
            }
            <div class="summary-row total">
              <span>Total Amount:</span>
              <span class="total-price">\${{ totalAmount().toFixed(2) }}</span>
            </div>
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
      margin-top: 20px;
      padding-top: 10px;
      border-top: 2px solid #333;
    }
    .summary-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 5px;
    }
    .summary-row.discount {
      color: #2e7d32;
    }
    .summary-row.total {
      font-size: 1.2rem;
      font-weight: bold;
      margin-top: 10px;
    }
  `]
})
export class BillDialogComponent implements OnInit {
  private restaurantService = inject(RestaurantService);
  private dialogRef = inject(MatDialogRef<BillDialogComponent>);
  
  orders = signal<OrderDetails[]>([]);
  loading = signal(true);
  subtotal = signal(0);
  discount = signal(0);
  totalAmount = signal(0);

  constructor(@Inject(MAT_DIALOG_DATA) public data: { tableId: number, tableNumber: number }) {}

  ngOnInit() {
    this.restaurantService.getTableBill(this.data.tableId).subscribe({
      next: (orders) => {
        this.orders.set(orders);
        const sub = orders.reduce((sum, order) => sum + order.totalAmount, 0);
        this.subtotal.set(sub);
        
        // Check if any order has a customer attached (assuming if one does, the whole table gets discount for now, 
        // or we calculate per order. Let's calculate per order if we want to be precise, 
        // but usually table bill is unified. 
        // Let's check if ANY order has a customerId, apply 10% to the whole bill for simplicity 
        // or just to the orders that have it.
        // The backend doesn't return customerId in OrderDetails yet? I added it to DTO.
        
        const hasMember = orders.some(o => !!o.customerId);
        const disc = hasMember ? sub * 0.10 : 0;
        
        this.discount.set(disc);
        this.totalAmount.set(sub - disc);
        
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
