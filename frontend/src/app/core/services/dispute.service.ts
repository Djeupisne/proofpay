import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Dispute } from '../models/dispute.model';

@Injectable({ providedIn: 'root' })
export class DisputeService {
  private readonly baseUrl = `${environment.apiUrl}`;

  constructor(private http: HttpClient) {}

  open(transactionId: string, openedBy: string, reasonCode: string, reasonDetails?: string): Observable<Dispute> {
    return this.http.post<Dispute>(`${this.baseUrl}/transactions/${transactionId}/open-dispute`, {
      openedBy, reasonCode, reasonDetails
    });
  }

  decide(disputeId: string, adminId: string, decisionCode: string, comment?: string): Observable<Dispute> {
    return this.http.post<Dispute>(`${this.baseUrl}/disputes/${disputeId}/decision`, {
      adminId, decisionCode, comment
    });
  }
}
