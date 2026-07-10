import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TransactionService } from '../../../core/services/transaction.service';
import { Transaction } from '../../../core/models/transaction.model';

@Component({
  selector: 'pp-transaction-success',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="success-page">
      <div class="success-card">
        <div class="success-icon">🎉</div>
        <h1>Paiement réussi !</h1>
        <p>Votre paiement a été effectué avec succès.</p>

        @if (transaction) {
          <div class="tx-summary">
            <p><strong>Transaction</strong> {{ transaction.publicRef }}</p>
            <p><strong>Montant</strong> {{ transaction.amount | currency:'XOF' }}</p>
            <p><strong>Statut</strong> <span class="badge-success">Payé</span></p>
          </div>
        }

        <div class="actions">
          <a routerLink="/transactions/{{ transaction?.id }}" class="btn btn-primary">
            Voir la transaction
          </a>
          <a routerLink="/transactions" class="btn btn-secondary">
            Mes transactions
          </a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .success-page {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 80vh;
      padding: 24px;
    }
    .success-card {
      background: var(--pp-color-surface);
      backdrop-filter: blur(16px);
      -webkit-backdrop-filter: blur(16px);
      border: 1px solid var(--pp-color-border);
      padding: 48px;
      border-radius: var(--pp-radius);
      text-align: center;
      max-width: 500px;
      box-shadow: var(--pp-shadow-md);
    }
    .success-icon {
      font-size: 64px;
      margin-bottom: 16px;
      filter: drop-shadow(0 0 16px rgba(34, 197, 94, 0.45));
    }
    .success-card h1 {
      margin: 0 0 8px;
      background: var(--pp-gradient-brand);
      -webkit-background-clip: text;
      background-clip: text;
      color: transparent;
    }
    .success-card p { color: var(--pp-color-text-muted); }
    .tx-summary {
      margin: 24px 0;
      padding: 16px;
      background: rgba(255, 255, 255, 0.03);
      border: 1px solid var(--pp-color-border);
      border-radius: var(--pp-radius-sm);
      text-align: left;
    }
    .tx-summary p { margin: 8px 0; color: var(--pp-color-text); }
    .badge-success {
      background: var(--pp-color-accent-light);
      color: var(--pp-color-accent);
      border: 1px solid rgba(5, 150, 105, 0.3);
      padding: 4px 12px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 600;
    }
    .actions {
      display: flex;
      gap: 12px;
      justify-content: center;
      flex-wrap: wrap;
    }
    .btn {
      padding: 12px 24px;
      border: none;
      border-radius: var(--pp-radius-sm);
      font-size: 14px;
      font-weight: 600;
      cursor: pointer;
      text-decoration: none;
      display: inline-block;
      transition: transform 0.12s ease, box-shadow 0.2s ease;
    }
    .btn-primary {
      background: var(--pp-gradient-brand);
      color: white;
      box-shadow: var(--pp-glow-primary);
    }
    .btn-primary:hover { transform: translateY(-1px); }
    .btn-secondary {
      background: rgba(255, 255, 255, 0.06);
      border: 1px solid var(--pp-color-border);
      color: var(--pp-color-text);
    }
    .btn-secondary:hover { border-color: var(--pp-color-accent); }
  `]
})
export class TransactionSuccessComponent implements OnInit {
  transaction: Transaction | null = null;

  constructor(
    private route: ActivatedRoute,
    private transactionService: TransactionService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.transactionService.getById(id).subscribe({
      next: (tx) => this.transaction = tx
    });
  }
}