import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RestaurantService } from '../../core/services/restaurant.service';
import { LocationService } from '../../core/services/location.service';
import { Restaurant } from '../../core/models/restaurant.types';

interface RestaurantWithDistance extends Restaurant {
  distance?: number;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  private restaurantService = inject(RestaurantService);
  private locationService = inject(LocationService);
  
  restaurants = signal<RestaurantWithDistance[]>([]);
  isLoadingLocation = signal<boolean>(false);
  userLocation = signal<{lat: number, lng: number} | null>(null);

  ngOnInit() {
    this.restaurantService.getRestaurants().subscribe(restaurants => {
      this.restaurants.set(restaurants);
      this.getUserLocation();
    });
  }

  async getUserLocation() {
    this.isLoadingLocation.set(true);
    try {
      const position = await this.locationService.getCurrentPosition();
      const lat = position.coords.latitude;
      const lng = position.coords.longitude;
      this.userLocation.set({ lat, lng });
      
      this.restaurants.update(current => {
        return current.map(r => {
          if (r.latitude && r.longitude) {
            return {
              ...r,
              distance: this.locationService.calculateDistance(lat, lng, r.latitude, r.longitude)
            };
          }
          return r;
        }).sort((a, b) => {
          if (a.distance !== undefined && b.distance !== undefined) {
            return a.distance - b.distance;
          }
          return 0; // Keep original order if distance is unknown
        });
      });
    } catch (error) {
      console.error('Error getting location', error);
    } finally {
      this.isLoadingLocation.set(false);
    }
  }
}
