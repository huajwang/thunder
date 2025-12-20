import { HttpClient } from '@angular/common/http';
import { inject, Injectable, NgZone } from '@angular/core';
import { map, Observable, switchMap } from 'rxjs';
import { Category, MenuItem, OrderDetails, OrderRequest, OrderResponse, Restaurant, RestaurantTable } from '../models/restaurant.types';

@Injectable({
  providedIn: 'root'
})
export class RestaurantService {
  private http = inject(HttpClient);
  private zone = inject(NgZone);
  private readonly API_URL = 'http://localhost:8080/api';

  placeOrder(order: OrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(`${this.API_URL}/orders`, order);
  }

  getOrders(restaurantId: number, statuses?: string[]): Observable<OrderDetails[]> {
    let params = `restaurantId=${restaurantId}`;
    if (statuses && statuses.length > 0) {
      params += `&statuses=${statuses.join(',')}`;
    }
    return this.http.get<OrderDetails[]>(`${this.API_URL}/orders?${params}`);
  }

  getOrderStream(restaurantId: number): Observable<any> {
    return new Observable(observer => {
      const token = localStorage.getItem('auth_token');
      const eventSource = new EventSource(`${this.API_URL}/orders/stream?restaurantId=${restaurantId}&token=${token}`);
      
      eventSource.onmessage = (event) => {
        this.zone.run(() => {
          observer.next(JSON.parse(event.data));
        });
      };

      eventSource.onerror = (error) => {
        this.zone.run(() => {
          observer.error(error);
        });
      };

      return () => {
        eventSource.close();
      };
    });
  }

  updateOrderStatus(orderId: number, status: string): Observable<OrderResponse> {
    return this.http.put<OrderResponse>(`${this.API_URL}/orders/${orderId}/status`, { status });
  }

  getTables(restaurantId: number): Observable<RestaurantTable[]> {
    return this.http.get<RestaurantTable[]>(`${this.API_URL}/tables?restaurantId=${restaurantId}`);
  }

  getTableBill(tableId: number): Observable<OrderDetails[]> {
    return this.http.get<OrderDetails[]>(`${this.API_URL}/tables/${tableId}/bill`);
  }

  applyMember(tableId: number, customerId: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/tables/${tableId}/apply-member`, { customerId });
  }

  checkoutTable(tableId: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/tables/${tableId}/checkout`, {});
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

  searchMenuItems(restaurantId: number, query: string): Observable<MenuItem[]> {
    return this.http.get<MenuItem[]>(`${this.API_URL}/restaurants/${restaurantId}/menu-items/search?q=${query}`);
  }

  getVipConfig(restaurantId: number): Observable<any> {
    return this.http.get<any>(`${this.API_URL}/restaurants/${restaurantId}/vip-config`);
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
