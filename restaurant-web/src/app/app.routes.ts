import { Routes } from '@angular/router';
import { RestaurantMenuComponent } from './features/restaurant-menu/restaurant-menu.component';

export const routes: Routes = [
  { path: ':slug', component: RestaurantMenuComponent },
  { path: ':slug/table/:tableNumber', component: RestaurantMenuComponent }
];


