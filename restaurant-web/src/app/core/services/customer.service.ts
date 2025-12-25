import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { Customer, RewardPointTransaction } from '../models/restaurant.types';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api/customers';

  enroll(restaurantId: number, phoneNumber: string): Observable<Customer> {
    return this.http.post<Customer>(`${this.API_URL}/enroll?restaurantId=${restaurantId}`, { phoneNumber });
  }

  requestLoginCode(restaurantId: number, phoneNumber: string): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/login/request-code?restaurantId=${restaurantId}`, { phoneNumber });
  }

  login(restaurantId: number, phoneNumber: string, code: string): Observable<Customer> {
    return this.http.post<any>(
      `${this.API_URL}/login?restaurantId=${restaurantId}`, 
      { phoneNumber, code }
    ).pipe(
      map(response => ({
        id: response.customerId,
        restaurantId: restaurantId,
        phoneNumber: response.phoneNumber,
        isMember: response.isMember,
        totalRewardPoints: response.totalRewardPoints
      }))
    );
  }

  getRewardHistory(customerId: number, restaurantId: number): Observable<RewardPointTransaction[]> {
    return this.http.get<RewardPointTransaction[]>(`${this.API_URL}/${customerId}/rewards?restaurantId=${restaurantId}`);
  }
}
