import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable, switchMap } from 'rxjs';
import { Category, MenuItem, OrderRequest, OrderResponse, Restaurant } from '../models/restaurant.types';

@Injectable({
  providedIn: 'root'
})
export class RestaurantService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api';

  placeOrder(order: OrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(`${this.API_URL}/orders`, order);
  }

  getRestaurantBySlug(slug: string): Observable<Restaurant> {
    return this.http.get<Restaurant>(`${this.API_URL}/restaurants/slug/${slug}`);
  }

  getCategories(restaurantId: number): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.API_URL}/restaurants/${restaurantId}/categories`);
  }

  getMenuItems(restaurantId: number): Observable<MenuItem[]> {
    return this.http.get<MenuItem[]>(`${this.API_URL}/restaurants/${restaurantId}/menu-items`);
  }

  // Helper to get the full menu structure
  getFullMenu(slug: string): Observable<{ restaurant: Restaurant, categories: Category[] }> {
    return this.getRestaurantBySlug(slug).pipe(
      switchMap(restaurant => {
        return this.http.get<Category[]>(`${this.API_URL}/restaurants/${restaurant.id}/categories`).pipe(
          switchMap(categories => {
            return this.http.get<MenuItem[]>(`${this.API_URL}/restaurants/${restaurant.id}/menu-items`).pipe(
              map(items => {
                // Map items to their categories
                const categoriesWithItems = categories.map(cat => ({
                  ...cat,
                  items: items.filter(item => item.categoryId === cat.id)
                }));
                return { restaurant, categories: categoriesWithItems };
              })
            );
          })
        );
      })
    );
  }
}
