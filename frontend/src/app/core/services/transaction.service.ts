import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CreateTransactionPayload, Transaction } from '../models/transaction.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly baseUrl = `${environment.apiUrl}/transactions`;

  constructor(private http: HttpClient) {}

  /** Historique paginé des transactions (achats + ventes) de l'utilisateur connecté. */
  listMine(userId: string, page = 0, size = 20): Observable<Page<Transaction>> {
    return this.http.get<Page<Transaction>>(this.baseUrl, { params: { userId, page, size } });
  }

  create(payload: CreateTransactionPayload): Observable<Transaction> {
    return this.http.post<Transaction>(this.baseUrl, payload);
  }

  getById(id: string): Observable<Transaction> {
    return this.http.get<Transaction>(`${this.baseUrl}/${id}`);
  }

  accept(id: string, sellerId: string): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.baseUrl}/${id}/accept`, {}, { params: { sellerId } });
  }

  reject(id: string, sellerId: string, reason?: string): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.baseUrl}/${id}/reject`, { reason }, { params: { sellerId } });
  }

  markDelivered(id: string, sellerId: string): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.baseUrl}/${id}/mark-delivered`, {}, { params: { sellerId } });
  }

  /** code : requis si confirmationMode est OTP ou CODE_SECRET (§8.5). */
  confirm(id: string, buyerId: string, code?: string): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.baseUrl}/${id}/confirm`, { code }, { params: { buyerId } });
  }
}
