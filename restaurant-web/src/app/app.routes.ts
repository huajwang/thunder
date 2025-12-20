import { Routes } from '@angular/router';
import { RestaurantMenuComponent } from './features/restaurant-menu/restaurant-menu.component';
import { LoginComponent } from './features/auth/login.component';
import { HomeComponent } from './features/home/home.component';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { 
    path: 'privacy-policy', 
    loadComponent: () => import('./features/privacy-policy/privacy-policy.component').then(m => m.PrivacyPolicyComponent)
  },
  { 
    path: ':slug/kitchen', 
    loadComponent: () => import('./features/kitchen/kitchen.component').then(m => m.KitchenComponent),
    canActivate: [authGuard]
  },
  { 
    path: ':slug/staff', 
    loadComponent: () => import('./features/staff/staff.component').then(m => m.StaffComponent),
    canActivate: [authGuard]
  },
  { 
    path: ':slug/tables', 
    loadComponent: () => import('./features/tables/tables.component').then(m => m.TablesComponent),
    canActivate: [authGuard]
  },
  { 
    path: ':slug/vip', 
    loadComponent: () => import('./features/vip-page/vip-page.component').then(m => m.VipPageComponent)
  },
  { 
    path: ':slug/search', 
    loadComponent: () => import('./features/search/search.component').then(m => m.SearchComponent)
  },
  { 
    path: ':slug/cart', 
    loadComponent: () => import('./features/cart-page/cart-page.component').then(m => m.CartPageComponent)
  },
  { 
    path: ':slug/location', 
    loadComponent: () => import('./features/location-page/location-page.component').then(m => m.LocationPageComponent)
  },
  { path: ':slug', component: RestaurantMenuComponent },
  { path: ':slug/table/:tableNumber', component: RestaurantMenuComponent }
];


