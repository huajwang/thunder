import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { CustomerService } from '../../core/services/customer.service';
import { RewardPointTransaction } from '../../core/models/restaurant.types';

@Component({
  selector: 'app-rewards-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatListModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>My Rewards</h2>
    <mat-dialog-content>
      <div class="total-points">
        <div class="points-value">{{ totalPoints }}</div>
        <div class="points-label">Total Points</div>
      </div>

      <h3>History</h3>
      @if (loading()) {
        <p>Loading history...</p>
      } @else if (history().length === 0) {
        <p>No history yet.</p>
      } @else {
        <mat-list>
          @for (item of history(); track item.id) {
            <mat-list-item>
              <mat-icon matListItemIcon [class.earned]="item.type === 'EARNED'" [class.redeemed]="item.type === 'REDEEMED'">
                {{ item.type === 'EARNED' ? 'add_circle' : 'remove_circle' }}
              </mat-icon>
              <div matListItemTitle>{{ item.description || item.type }}</div>
              <div matListItemLine>{{ item.createdAt | date:'medium' }}</div>
              <div matListItemMeta class="points-change" [class.positive]="item.points > 0">
                {{ item.points > 0 ? '+' : '' }}{{ item.points }}
              </div>
            </mat-list-item>
          }
        </mat-list>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Close</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .total-points {
      text-align: center;
      padding: 20px;
      background-color: #f5f5f5;
      border-radius: 8px;
      margin-bottom: 20px;
    }
    .points-value {
      font-size: 2.5rem;
      font-weight: bold;
      color: #1976d2;
    }
    .points-label {
      color: #666;
    }
    .earned { color: green; }
    .redeemed { color: orange; }
    .points-change { font-weight: bold; }
    .positive { color: green; }
  `]
})
export class RewardsDialogComponent implements OnInit {
  private customerService = inject(CustomerService);
  
  customerId!: number;
  restaurantId!: number;
  totalPoints: number = 0;
  
  history = signal<RewardPointTransaction[]>([]);
  loading = signal(true);

  ngOnInit() {
    if (this.customerId && this.restaurantId) {
      this.customerService.getRewardHistory(this.customerId, this.restaurantId).subscribe({
        next: (data) => {
          this.history.set(data);
          this.loading.set(false);
        },
        error: () => this.loading.set(false)
      });
    }
  }
}
