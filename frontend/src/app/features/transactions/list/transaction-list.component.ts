import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { StatusBadgeComponent } from '../../../shared/components/status-badge.component';
import { XofCurrencyPipe } from '../../../shared/pipes/xof-currency.pipe';
import { TransactionService } from '../../../core/services/transaction.service';
import { AuthService } from '../../../core/auth/auth.service';
import { Transaction } from '../../../core/models/transaction.model';

const PAGE_SIZE = 10;

/** Historique paginé des transactions de l'utilisateur connecté. */
@Component({
  selector: 'pp-transaction-list',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusBadgeComponent, XofCurrencyPipe],
  templateUrl: './transaction-list.component.html'
})
export class TransactionListComponent implements OnInit {
  transactions = signal<Transaction[]>([]);
  loading = signal(true);
  page = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);

  private userId: string | null;

  constructor(private transactionService: TransactionService, private auth: AuthService) {
    this.userId = this.auth.currentUserId();
  }

  ngOnInit(): void {
    this.load(0);
  }

  load(page: number): void {
    if (!this.userId) {
      this.loading.set(false);
      return;
    }
    this.loading.set(true);
    this.transactionService.listMine(this.userId, page, PAGE_SIZE).subscribe({
      next: (result) => {
        this.transactions.set(result.content);
        this.page.set(result.number);
        this.totalPages.set(result.totalPages);
        this.totalElements.set(result.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  previous(): void {
    if (this.page() > 0) this.load(this.page() - 1);
  }

  next(): void {
    if (this.page() < this.totalPages() - 1) this.load(this.page() + 1);
  }
}
