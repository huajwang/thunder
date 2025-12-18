import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { RestaurantService } from '../../core/services/restaurant.service';
import { RestaurantTable, OrderDetails } from '../../core/models/restaurant.types';
import { BillDialogComponent } from './bill-dialog.component';

@Component({
  selector: 'app-tables',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatDialogModule],
  template: `
    <div class="tables-container">
      <header class="tables-header">
        <h1>Table Management</h1>
        <button mat-raised-button color="primary" (click)="refreshTables()">
          <mat-icon>refresh</mat-icon> Refresh
        </button>
      </header>

      <div class="tables-grid">
        @for (table of tables(); track table.id) {
          <mat-card class="table-card" (click)="openBill(table)">
            <mat-card-content>
              <div class="table-number">{{ table.tableNumber }}</div>
              <div class="table-status">Click to View Bill</div>
            </mat-card-content>
          </mat-card>
        }
      </div>
    </div>
  `,
  styles: [`
    .tables-container {
      padding: 20px;
      background-color: #f5f5f5;
      min-height: 100vh;
    }
    .tables-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }
    .tables-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
      gap: 20px;
    }
    .table-card {
      cursor: pointer;
      transition: transform 0.2s;
      text-align: center;
      padding: 20px;
    }
    .table-card:hover {
      transform: scale(1.05);
      background-color: #e3f2fd;
    }
    .table-number {
      font-size: 2.5rem;
      font-weight: bold;
      color: #1976d2;
      margin-bottom: 10px;
    }
    .table-status {
      color: #757575;
      font-size: 0.9rem;
    }
  `]
})
export class TablesComponent implements OnInit {
  private restaurantService = inject(RestaurantService);
  private route = inject(ActivatedRoute);
  private dialog = inject(MatDialog);
  
  restaurantId: number | null = null;
  tables = signal<RestaurantTable[]>([]);

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const slug = params.get('slug');
      if (slug) {
        this.restaurantService.getRestaurantBySlug(slug).subscribe(restaurant => {
          this.restaurantId = restaurant.id;
          this.refreshTables();
        });
      }
    });
  }

  refreshTables() {
    if (!this.restaurantId) return;
    this.restaurantService.getTables(this.restaurantId).subscribe(tables => {
      this.tables.set(tables.sort((a, b) => a.tableNumber - b.tableNumber));
    });
  }

  openBill(table: RestaurantTable) {
    this.dialog.open(BillDialogComponent, {
      data: { tableId: table.id, tableNumber: table.tableNumber },
      width: '500px'
    });
  }
}
