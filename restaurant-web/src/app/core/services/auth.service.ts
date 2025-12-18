import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { tap } from 'rxjs';

export interface AuthResponse {
  token: string;
  restaurantId: number;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private readonly API_URL = 'http://localhost:8080/api/auth';
  private readonly TOKEN_KEY = 'auth_token';
  private readonly RESTAURANT_ID_KEY = 'restaurant_id';

  currentUser = signal<{ restaurantId: number, role: string } | null>(this.getUserFromStorage());

  login(username: string, password: string) {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, { username, password }).pipe(
      tap(response => {
        localStorage.setItem(this.TOKEN_KEY, response.token);
        localStorage.setItem(this.RESTAURANT_ID_KEY, response.restaurantId.toString());
        this.currentUser.set({ restaurantId: response.restaurantId, role: response.role });
      })
    );
  }

  logout() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.RESTAURANT_ID_KEY);
    this.currentUser.set(null);
    this.router.navigate(['/']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  private getUserFromStorage() {
    const token = this.getToken();
    const restaurantId = localStorage.getItem(this.RESTAURANT_ID_KEY);
    if (token && restaurantId) {
      // In a real app, decode token to get role
      return { restaurantId: parseInt(restaurantId), role: 'ADMIN' }; 
    }
    return null;
  }
}
