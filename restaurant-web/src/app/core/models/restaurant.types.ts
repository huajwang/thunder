export interface Restaurant {
  id: number;
  name: string;
  slug: string;
  description?: string;
  imageUrl?: string;
  address?: string;
  phoneNumber?: string;
  latitude?: number;
  longitude?: number;
}

export interface Category {
  id: number;
  restaurantId: number;
  name: string;
  displayOrder: number;
  items?: MenuItem[]; // Optional, might be populated by a join or separate call
}

export interface MenuItem {
  id: number;
  restaurantId: number;
  categoryId?: number;
  name: string;
  description?: string;
  price: number;
  imageUrl?: string;
  isAvailable: boolean;
}

export interface OrderItemRequest {
  menuItemId: number;
  quantity: number;
}

export interface OrderRequest {
  restaurantId: number;
  tableId?: number;
  customerId?: number;
  deliveryAddress?: string;
  items: OrderItemRequest[];
}

export interface OrderResponse {
  id: number;
  status: string;
  totalAmount: number;
}

export interface OrderItemDto {
  menuItemId: number;
  menuItemName: string;
  quantity: number;
  price: number;
}

export interface OrderDetails {
  id: number;
  restaurantId: number;
  tableId?: number;
  customerId?: number;
  status: string;
  subTotal: number;
  tax: number;
  discount: number;
  totalAmount: number;
  createdAt: string;
  updatedAt: string;
  items: OrderItemDto[];
}

export interface Customer {
  id: number;
  restaurantId: number;
  phoneNumber: string;
  isMember: boolean;
}

export interface RestaurantTable {
  id: number;
  restaurantId: number;
  tableNumber: number;
  qrCodeSlug?: string;
}
