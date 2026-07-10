import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { StatusBadgeComponent } from '../../../shared/components/status-badge.component';
import { XofCurrencyPipe } from '../../../shared/pipes/xof-currency.pipe';
import { environment } from '../../../../environments/environment';
import { Transaction } from '../../../core/models/transaction.model';

interface AdminStats {
  totalTransactions: number;
  totalUsers: number;
  openDisputes: number;
  suspendedUsers: number;
  blockedUsers: number;
  transactionsByStatus: Record<string, number>;
}

/** Tableau de bord support : vue d'ensemble + recherche (§8.8 spécifications fonctionnelles). */
@Component({
  selector: 'pp-admin-search',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, StatusBadgeComponent, XofCurrencyPipe],
  templateUrl: './admin-search.component.html'
})
export class AdminSearchComponent implements OnInit {
  publicRef = '';
  results = signal<Transaction[]>([]);
  searching = signal(false);
  searched = signal(false);

  stats = signal<AdminStats | null>(null);
  statsLoading = signal(true);

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<AdminStats>(`${environment.apiUrl}/admin/stats`).subscribe({
      next: (s) => { this.stats.set(s); this.statsLoading.set(false); },
      error: () => this.statsLoading.set(false)
    });
  }

  search(): void {
    if (!this.publicRef.trim()) return;
    this.searching.set(true);
    this.searched.set(true);
    this.http.get<Transaction[]>(`${environment.apiUrl}/admin/transactions`, {
      params: { publicRef: this.publicRef.trim() }
    }).subscribe({
      next: (txs) => { this.results.set(txs); this.searching.set(false); },
      error: () => { this.results.set([]); this.searching.set(false); }
    });
  }

  /** Nombre de transactions actuellement en cours (ni terminées, ni annulées/remboursées). */
  activeCount(s: AdminStats): number {
    const byStatus = s.transactionsByStatus;
    const done = new Set(['TERMINEE', 'ANNULEE', 'REMBOURSEE', 'REFUSEE', 'EXPIREE']);
    return Object.entries(byStatus)
      .filter(([status]) => !done.has(status))
      .reduce((sum, [, count]) => sum + count, 0);
  }
}
