import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Customer } from '../models/restaurant.types';

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

  login(restaurantId: number, phoneNumber: string, code: string): Observable<{ customerId: number, phoneNumber: string, isMember: boolean }> {
    return this.http.post<{ customerId: number, phoneNumber: string, isMember: boolean }>(
      `${this.API_URL}/login?restaurantId=${restaurantId}`, 
      { phoneNumber, code }
    );
  }
}
