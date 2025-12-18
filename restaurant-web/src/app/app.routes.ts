import { Routes } from '@angular/router';
import { RestaurantMenuComponent } from './features/restaurant-menu/restaurant-menu.component';

export const routes: Routes = [
  { 
    path: ':slug/kitchen', 
    loadComponent: () => import('./features/kitchen/kitchen.component').then(m => m.KitchenComponent) 
  },
  { 
    path: ':slug/staff', 
    loadComponent: () => import('./features/staff/staff.component').then(m => m.StaffComponent) 
  },
  { path: ':slug', component: RestaurantMenuComponent },
  { path: ':slug/table/:tableNumber', component: RestaurantMenuComponent }
];


