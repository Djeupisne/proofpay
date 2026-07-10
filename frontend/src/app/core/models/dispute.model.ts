export type DisputeStatus = 'OPEN' | 'UNDER_REVIEW' | 'RESOLVED' | 'REJECTED';

export interface Dispute {
  id: string;
  transactionId: string;
  openedBy: string;
  reasonCode: string;
  reasonDetails?: string;
  status: DisputeStatus;
  decisionCode?: string;
  decisionComment?: string;
}
