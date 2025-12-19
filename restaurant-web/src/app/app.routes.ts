import { Routes } from '@angular/router';
import { RestaurantMenuComponent } from './features/restaurant-menu/restaurant-menu.component';
import { LoginComponent } from './features/auth/login.component';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
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
  { path: ':slug', component: RestaurantMenuComponent },
  { path: ':slug/table/:tableNumber', component: RestaurantMenuComponent }
];


