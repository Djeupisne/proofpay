export type UserStatus = 'PENDING_VERIFICATION' | 'ACTIVE' | 'SUSPENDED' | 'BLOCKED';
export type UserRole = 'USER' | 'ADMIN' | 'SUPPORT';

export interface User {
  id: string;
  phone: string;
  firstName?: string;
  lastName?: string;
  displayName?: string;
  email?: string;
  preferredLanguage?: string;
  status: UserStatus;
  role: UserRole;
  rating?: number;
  transactionsCount?: number;
}

export interface UpdateProfilePayload {
  firstName?: string;
  lastName?: string;
  email?: string;
  preferredLanguage?: string;
}
