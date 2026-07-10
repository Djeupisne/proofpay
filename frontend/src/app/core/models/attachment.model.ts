export interface Attachment {
  id: string;
  ownerType: 'TRANSACTION' | 'DISPUTE';
  ownerId: string;
  originalName: string;
  mimeType: string;
  sizeBytes: number;
  uploadedBy: string;
  createdAt: string;
}
