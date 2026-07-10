export type TransactionStatus =
  | 'BROUILLON' | 'EN_ATTENTE_ACCEPTATION' | 'REFUSEE' | 'EN_ATTENTE_PAIEMENT'
  | 'PAYE' | 'EN_LIVRAISON' | 'A_CONFIRMER' | 'RELACHE_AUTO' | 'LITIGE'
  | 'TERMINEE' | 'REMBOURSEE' | 'ANNULEE' | 'EXPIREE';

export type ConfirmationMode = 'BUTTON' | 'OTP' | 'CODE_SECRET';

export interface Transaction {
  id: string;
  publicRef: string;
  buyerId: string;
  sellerId: string;
  title: string;
  description?: string;
  categoryCode?: string;
  currency: string;
  amount: number;
  fees: number;
  totalAmount?: number;
  status: TransactionStatus;
  confirmationMode: ConfirmationMode;
  deliveryDeadline?: string;
  autoReleaseAt?: string;
  createdAt: string;
  updatedAt?: string;

  // 🔥 AJOUTER CES CHAMPS POUR LA TIMELINE
  paidAt?: string | null;
  deliveredAt?: string | null;
  confirmedAt?: string | null;
  completedAt?: string | null;
  cancelledAt?: string | null;
}

export interface CreateTransactionPayload {
  buyerId: string;
  sellerPhone: string;
  title: string;
  description?: string;
  categoryCode?: string;
  amount: number;
  confirmationMode: ConfirmationMode;
  confirmationSecret?: string;
  deliveryDelayHours?: number;
}