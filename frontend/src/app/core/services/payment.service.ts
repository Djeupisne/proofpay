// payment.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface PaymentResponse {
  success: boolean;
  message: string;
  paymentId: string;
  transactionId: string;
  amount: number;
  status: string;
  paidAt: string;
  redirectUrl: string;
}

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private readonly baseUrl = `${environment.apiUrl}/transactions`;

  constructor(private http: HttpClient) {}

  pay(transactionId: string, payerPhone: string): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`${this.baseUrl}/${transactionId}/pay`, { payerPhone });
  }

  getPaymentStatus(transactionId: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/${transactionId}/payment-status`);
  }
}