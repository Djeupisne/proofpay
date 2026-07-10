import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AdminSetting } from '../models/admin-setting.model';
import { Dispute } from '../models/dispute.model';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly baseUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  listSettings(): Observable<AdminSetting[]> {
    return this.http.get<AdminSetting[]>(`${this.baseUrl}/settings`);
  }

  updateSetting(key: string, value: string): Observable<AdminSetting> {
    return this.http.put<AdminSetting>(`${this.baseUrl}/settings/${key}`, { value });
  }

  listOpenDisputes(): Observable<Dispute[]> {
    return this.http.get<Dispute[]>(`${this.baseUrl}/disputes`);
  }
}
