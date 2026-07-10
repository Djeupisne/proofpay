import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { TransactionService } from '../../../core/services/transaction.service';
import { AuthService } from '../../../core/auth/auth.service';
import { ConfirmationMode } from '../../../core/models/transaction.model';

/** UC-01 : créer une transaction (§8.2 spécifications fonctionnelles). */
@Component({
  selector: 'pp-transaction-create',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './transaction-create.component.html'
})
export class TransactionCreateComponent {
  title = '';
  description = '';
  categoryCode = '';
  amount: number | null = null;
  sellerPhone = '';
  confirmationMode: ConfirmationMode = 'BUTTON';
  confirmationSecret = '';
  deliveryDelayHours: number | null = null;

  submitting = signal(false);
  errorMessage = signal<string | null>(null);

  constructor(
    private transactionService: TransactionService,
    private authService: AuthService,
    private router: Router
  ) {}

  submit(): void {
    const buyerId = this.authService.currentUserId();
    if (!buyerId || !this.amount) {
      this.errorMessage.set('Merci de compléter le formulaire.');
      return;
    }
    if (this.confirmationMode === 'CODE_SECRET' && !this.confirmationSecret) {
      this.errorMessage.set('Un code secret est requis pour ce mode de confirmation.');
      return;
    }

    this.submitting.set(true);
    this.transactionService.create({
      buyerId,
      sellerPhone: this.sellerPhone,
      title: this.title,
      description: this.description,
      categoryCode: this.categoryCode || undefined,
      amount: this.amount,
      confirmationMode: this.confirmationMode,
      confirmationSecret: this.confirmationMode === 'CODE_SECRET' ? this.confirmationSecret : undefined,
      deliveryDelayHours: this.deliveryDelayHours ?? undefined
    }).subscribe({
      next: (tx) => this.router.navigate(['/transactions', tx.id]),
      error: (err: HttpErrorResponse) => {
        this.errorMessage.set(err?.error?.message ?? 'Impossible de créer la transaction.');
        this.submitting.set(false);
      }
    });
  }
}
