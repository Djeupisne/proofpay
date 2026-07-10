import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UpdateProfilePayload, User } from '../models/user.model';

export interface PublicProfile {
  id: string;
  displayName: string;
  verified: boolean;
  rating: number | null;
  transactionsCount: number;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly baseUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  me(): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/me`);
  }

  updateMe(payload: UpdateProfilePayload): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/me`, payload);
  }

  /** Identité vérifiée + réputation d'un autre utilisateur (acheteur/vendeur d'une transaction). */
  publicProfile(userId: string): Observable<PublicProfile> {
    return this.http.get<PublicProfile>(`${this.baseUrl}/${userId}/public-profile`);
  }
}
