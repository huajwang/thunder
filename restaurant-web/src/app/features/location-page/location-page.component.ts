import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { RestaurantService } from '../../core/services/restaurant.service';
import { Restaurant } from '../../core/models/restaurant.types';

@Component({
  selector: 'app-location-page',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="location-container">
      @if (loading()) {
        <div class="loading-spinner">
          <mat-progress-spinner mode="indeterminate"></mat-progress-spinner>
        </div>
      } @else if (restaurant(); as rest) {
        <mat-card class="location-card">
          <div class="header-row">
            <button mat-icon-button (click)="goBack()">
              <mat-icon>arrow_back</mat-icon>
            </button>
            <div class="header-text">
              <mat-card-title>{{ rest.name }}</mat-card-title>
              <mat-card-subtitle>{{ rest.address }}</mat-card-subtitle>
            </div>
          </div>
          
          <mat-card-content>
            <div class="map-container">
              @if (mapUrl()) {
                <iframe
                  [src]="mapUrl()"
                  width="100%"
                  height="450"
                  style="border:0;"
                  allowfullscreen=""
                  loading="lazy"
                  referrerpolicy="no-referrer-when-downgrade">
                </iframe>
              } @else {
                <div class="no-map">
                  <mat-icon>map</mat-icon>
                  <p>Map location not available</p>
                </div>
              }
            </div>

            <div class="info-section">
              <h3><mat-icon>schedule</mat-icon> Business Hours</h3>
              <div class="hours-list">
                <p>{{ rest.businessHours || 'Mon-Sun: 10:00 AM - 10:00 PM' }}</p>
              </div>

              @if (rest.phoneNumber) {
                <h3><mat-icon>phone</mat-icon> Contact</h3>
                <p>{{ rest.phoneNumber }}</p>
              }
            </div>
          </mat-card-content>
        </mat-card>
      } @else {
        <div class="error-message">
          <p>Restaurant not found.</p>
        </div>
      }
    </div>
  `,
  styles: [`
    .location-container {
      max-width: 800px;
      margin: 20px auto;
      padding: 16px;
    }
    .loading-spinner {
      display: flex;
      justify-content: center;
      padding: 40px;
    }
    .location-card {
      margin-bottom: 20px;
    }
    .header-row {
      display: flex;
      align-items: center;
      padding: 16px 16px 0;
      gap: 8px;
    }
    .header-text {
      display: flex;
      flex-direction: column;
    }
    .map-container {
      margin: 20px 0;
      border-radius: 8px;
      overflow: hidden;
      background: #f5f5f5;
    }
    .no-map {
      height: 300px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #888;
    }
    .no-map mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 10px;
    }
    .info-section {
      margin-top: 24px;
    }
    .info-section h3 {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #333;
      margin-bottom: 12px;
    }
    .hours-list, .contact-info {
      padding-left: 32px;
      color: #666;
      font-size: 16px;
      line-height: 1.6;
    }
  `]
})
export class LocationPageComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private restaurantService = inject(RestaurantService);
  private sanitizer = inject(DomSanitizer);
  private location = inject(Location);

  restaurant = signal<Restaurant | null>(null);
  loading = signal<boolean>(true);
  mapUrl = signal<SafeResourceUrl | null>(null);

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const slug = params.get('slug');
      if (slug) {
        this.loadRestaurant(slug);
      }
    });
  }

  goBack() {
    this.location.back();
  }

  private loadRestaurant(slug: string) {
    this.loading.set(true);
    this.restaurantService.getRestaurantBySlug(slug).subscribe({
      next: (data) => {
        this.restaurant.set(data);
        this.generateMapUrl(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading restaurant', err);
        this.loading.set(false);
      }
    });
  }

  private generateMapUrl(restaurant: Restaurant) {
    if (restaurant.latitude && restaurant.longitude) {
      // Using Google Maps Embed API with coordinates
      // Note: In a production environment, you should use a valid API Key.
      // For this demo, we can use the 'q' parameter with coordinates.
      // Since we don't have a key, we might use the simple maps output embed which is less restrictive but technically legacy.
      // Or we can use the query search.
      
      // Using the "search" mode which sometimes works without a key for simple queries, or just standard maps link.
      // But for iframe embed, a key is usually required for the v1/place endpoint.
      // Let's try the simple output=embed format which is often used as a workaround.
      const url = `https://maps.google.com/maps?q=${restaurant.latitude},${restaurant.longitude}&t=&z=15&ie=UTF8&iwloc=&output=embed`;
      this.mapUrl.set(this.sanitizer.bypassSecurityTrustResourceUrl(url));
    } else if (restaurant.address) {
       const encodedAddress = encodeURIComponent(restaurant.address);
       const url = `https://maps.google.com/maps?q=${encodedAddress}&t=&z=15&ie=UTF8&iwloc=&output=embed`;
       this.mapUrl.set(this.sanitizer.bypassSecurityTrustResourceUrl(url));
    }
  }
}
