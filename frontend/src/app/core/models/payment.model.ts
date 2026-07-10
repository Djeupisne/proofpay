export type PaymentStatus = 'INITIATED' | 'PENDING' | 'CONFIRMED' | 'FAILED' | 'REVERSED';

export interface Payment {
  id: string;
  transactionId: string;
  providerCode: string;
  requestRef: string;
  providerRef?: string;
  amount: number;
  currency: string;
  status: PaymentStatus;
}
