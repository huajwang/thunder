import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

export interface AddressSuggestion {
  place_id: number;
  display_name: string;
  lat: string;
  lon: string;
  address?: any;
}

@Injectable({
  providedIn: 'root'
})
export class AddressService {
  private http = inject(HttpClient);
  private readonly NOMINATIM_URL = 'https://nominatim.openstreetmap.org/search';
  private readonly NOMINATIM_REVERSE_URL = 'https://nominatim.openstreetmap.org/reverse';

  searchAddress(query: string, postalCode?: string): Observable<AddressSuggestion[]> {
    if ((!query || query.length < 3) && (!postalCode || postalCode.length < 3)) {
      return of([]);
    }

    const params: any = {
      format: 'json',
      addressdetails: '1',
      limit: '5',
      countrycodes: 'ca,us,mx' // Prioritize North America
    };

    if (query) {
      params.q = postalCode ? `${query}, ${postalCode}` : query;
    } else if (postalCode) {
      params.postalcode = postalCode;
    }

    return this.http.get<any[]>(this.NOMINATIM_URL, { params }).pipe(
      map(results => results.map(r => ({
        place_id: r.place_id,
        display_name: r.display_name,
        lat: r.lat,
        lon: r.lon
      }))),
      catchError(() => of([]))
    );
  }

  searchByPostalCode(postalCode: string): Observable<AddressSuggestion[]> {
     // Use the specific postal code search
     return this.searchAddress('', postalCode);
  }

  reverseGeocode(lat: number, lon: number): Observable<AddressSuggestion | null> {
    const params = {
      format: 'json',
      lat: lat.toString(),
      lon: lon.toString(),
      addressdetails: '1'
    };

    return this.http.get<any>(this.NOMINATIM_REVERSE_URL, { params }).pipe(
      map(r => {
        if (!r || !r.display_name) return null;
        return {
          place_id: r.place_id,
          display_name: r.display_name,
          lat: r.lat,
          lon: r.lon,
          address: r.address // Return full address details if needed
        } as AddressSuggestion;
      }),
      catchError(() => of(null))
    );
  }
}
