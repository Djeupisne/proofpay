import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TransactionService } from '../../../core/services/transaction.service';
import { StatusBadgeComponent } from '../../../shared/components/status-badge.component';
import { AttachmentListComponent } from '../../../shared/components/attachment-list.component';
import { XofCurrencyPipe } from '../../../shared/pipes/xof-currency.pipe';
import { PaymentService } from '../../../core/services/payment.service';
import { AuthService } from '../../../core/auth/auth.service';
import { UserService, PublicProfile } from '../../../core/services/user.service';
import { Transaction } from '../../../core/models/transaction.model';

@Component({
  selector: 'pp-transaction-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, StatusBadgeComponent, AttachmentListComponent, XofCurrencyPipe],
  templateUrl: './transaction-detail.component.html'
  // ❌ SUPPRIMEZ CETTE LIGNE :
  // styleUrls: ['./transaction-detail.component.css']
})
export class TransactionDetailComponent implements OnInit {
  transaction = signal<Transaction | null>(null);
  loading = signal(true);
  paying = signal(false);
  actionError = signal<string | null>(null);
  actionSuccess = signal<string | null>(null);
  confirmationCode = '';
  currentUserId: string | null;

  /** Identité de la contrepartie (vendeur si je suis acheteur, ou inversement) — voir UserService. */
  counterpartProfile = signal<PublicProfile | null>(null);
  counterpartRole = signal<'Vendeur' | 'Acheteur' | null>(null);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private transactionService: TransactionService,
    private paymentService: PaymentService,
    private auth: AuthService,
    private userService: UserService
  ) {
    this.currentUserId = this.auth.currentUserId();
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.load(id);
  }

  private load(id: string): void {
    this.loading.set(true);
    this.actionError.set(null);
    this.actionSuccess.set(null);

    this.transactionService.getById(id).subscribe({
      next: (tx) => {
        this.transaction.set(tx);
        this.loading.set(false);
        this.loadCounterpart(tx);
      },
      error: (err) => {
        console.error('Erreur de chargement:', err);
        this.loading.set(false);
        this.actionError.set('Impossible de charger la transaction.');
      }
    });
  }

  /** Affiche l'identité et la réputation de l'autre partie — signal de confiance essentiel
   * pour une application qui vise à éviter les arnaques entre particuliers. */
  private loadCounterpart(tx: Transaction): void {
    const isBuyer = tx.buyerId === this.currentUserId;
    const counterpartId = isBuyer ? tx.sellerId : tx.buyerId;
    this.counterpartRole.set(isBuyer ? 'Vendeur' : 'Acheteur');
    this.userService.publicProfile(counterpartId).subscribe({
      next: (profile) => this.counterpartProfile.set(profile),
      error: () => this.counterpartProfile.set(null)
    });
  }

  accept(): void {
    const tx = this.transaction();
    if (!tx || !this.currentUserId) return;

    this.actionError.set(null);
    this.actionSuccess.set(null);

    this.transactionService.accept(tx.id, this.currentUserId).subscribe({
      next: (updated) => {
        this.transaction.set(updated);
        this.actionSuccess.set('✅ Transaction acceptée avec succès !');
        setTimeout(() => this.actionSuccess.set(null), 5000);
      },
      error: (err) => {
        this.actionError.set('❌ Impossible d\'accepter la transaction.');
        setTimeout(() => this.actionError.set(null), 5000);
      }
    });
  }

  reject(): void {
    const tx = this.transaction();
    if (!tx || !this.currentUserId) return;

    this.actionError.set(null);
    this.actionSuccess.set(null);

    this.transactionService.reject(tx.id, this.currentUserId).subscribe({
      next: (updated) => {
        this.transaction.set(updated);
        this.actionSuccess.set('❌ Transaction refusée.');
        setTimeout(() => this.actionSuccess.set(null), 5000);
      },
      error: () => {
        this.actionError.set('❌ Impossible de refuser la transaction.');
        setTimeout(() => this.actionError.set(null), 5000);
      }
    });
  }

  pay(): void {
    const tx = this.transaction();
    if (!tx || !this.currentUserId) {
      this.actionError.set('❌ Vous devez être connecté.');
      return;
    }

    this.paying.set(true);
    this.actionError.set(null);
    this.actionSuccess.set(null);

    console.log('💳 Paiement en cours pour:', tx.id);

    this.paymentService.pay(tx.id, this.currentUserId).subscribe({
      next: (response: any) => {
        console.log('✅ Paiement réussi:', response);
        this.paying.set(false);
        this.actionSuccess.set('✅ ' + (response.message || 'Paiement effectué avec succès'));
        this.load(tx.id);
      },
      error: (err) => {
        console.error('❌ Erreur de paiement:', err);
        this.paying.set(false);

        let errorMessage = '❌ Paiement impossible pour le moment.';
        if (err.error && err.error.message) {
          errorMessage = '❌ ' + err.error.message;
        }
        this.actionError.set(errorMessage);
        setTimeout(() => this.actionError.set(null), 5000);
      }
    });
  }

  markDelivered(): void {
    const tx = this.transaction();
    if (!tx || !this.currentUserId) return;

    this.actionError.set(null);
    this.actionSuccess.set(null);

    this.transactionService.markDelivered(tx.id, this.currentUserId).subscribe({
      next: (updated) => {
        this.transaction.set(updated);
        this.actionSuccess.set('📦 Livraison déclarée !');
        setTimeout(() => this.actionSuccess.set(null), 5000);
      },
      error: () => {
        this.actionError.set('❌ Impossible de déclarer la livraison.');
        setTimeout(() => this.actionError.set(null), 5000);
      }
    });
  }

  confirm(): void {
    const tx = this.transaction();
    if (!tx || !this.currentUserId) return;

    if (tx.confirmationMode !== 'BUTTON' && !this.confirmationCode) {
      this.actionError.set('❌ Veuillez entrer le code de confirmation.');
      setTimeout(() => this.actionError.set(null), 5000);
      return;
    }

    this.actionError.set(null);
    this.actionSuccess.set(null);

    this.transactionService.confirm(tx.id, this.currentUserId, this.confirmationCode || undefined).subscribe({
      next: (updated) => {
        this.transaction.set(updated);
        this.confirmationCode = '';
        this.actionSuccess.set('✅ Réception confirmée !');
        setTimeout(() => this.actionSuccess.set(null), 5000);
      },
      error: () => {
        this.actionError.set('❌ Code invalide ou confirmation impossible.');
        setTimeout(() => this.actionError.set(null), 5000);
      }
    });
  }

  openDispute(): void {
    const tx = this.transaction();
    if (!tx) return;
    this.router.navigate(['/transactions', tx.id, 'dispute']);
  }
}