import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

// 🔥 NOUVELLE INTERFACE pour la demande d'OTP
interface RequestOtpRequest {
  phone: string;
  email?: string;      // Optionnel pour SMS
  channel?: 'SMS' | 'EMAIL'; // Canal choisi
}

interface VerifyOtpResponse {
  accessToken: string;
  userId: string;
  role: string;
}

const TOKEN_KEY = 'proofpay_access_token';
const USER_ID_KEY = 'proofpay_user_id';
const ROLE_KEY = 'proofpay_role';

/**
 * Authentification par numéro + OTP (§8.1 spécifications fonctionnelles).
 * Le token est conservé en mémoire (signal) + localStorage pour la persistance
 * de session ; à durcir avec un refresh token en production.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly currentUserId = signal<string | null>(localStorage.getItem(USER_ID_KEY));
  readonly currentRole = signal<string | null>(localStorage.getItem(ROLE_KEY));
  readonly isAuthenticated = signal<boolean>(!!localStorage.getItem(TOKEN_KEY));

  constructor(private http: HttpClient) {}

  // 🔥 MODIFICATION : Ajout de l'email et du canal
  requestOtp(phone: string, email?: string, channel: 'SMS' | 'EMAIL' = 'SMS'): Observable<{ message: string }> {
    const payload: RequestOtpRequest = { phone };
    if (email) {
      payload.email = email;
    }
    payload.channel = channel;
    
    return this.http.post<{ message: string }>(`${environment.apiUrl}/auth/request-otp`, payload);
  }

  verifyOtp(phone: string, code: string): Observable<VerifyOtpResponse> {
    return this.http.post<VerifyOtpResponse>(`${environment.apiUrl}/auth/verify-otp`, { phone, code }).pipe(
      tap(response => {
        localStorage.setItem(TOKEN_KEY, response.accessToken);
        localStorage.setItem(USER_ID_KEY, response.userId);
        localStorage.setItem(ROLE_KEY, response.role);
        this.currentUserId.set(response.userId);
        this.currentRole.set(response.role);
        this.isAuthenticated.set(true);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_ID_KEY);
    localStorage.removeItem(ROLE_KEY);
    this.currentUserId.set(null);
    this.currentRole.set(null);
    this.isAuthenticated.set(false);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isAdmin(): boolean {
    return this.currentRole() === 'ADMIN';
  }
}
