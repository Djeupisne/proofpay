import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { DisputeService } from '../../../core/services/dispute.service';
import { AuthService } from '../../../core/auth/auth.service';
import { AttachmentListComponent } from '../../../shared/components/attachment-list.component';
import { Dispute } from '../../../core/models/dispute.model';

/** UC-07 : arbitrer un litige (§8.6 spécifications fonctionnelles). */
@Component({
  selector: 'pp-dispute-decision',
  standalone: true,
  imports: [CommonModule, FormsModule, AttachmentListComponent],
  templateUrl: './dispute-decision.component.html'
})
export class DisputeDecisionComponent implements OnInit {
  disputes = signal<Dispute[]>([]);
  loading = signal(true);
  decidingId = signal<string | null>(null);
  errorMessage = signal<string | null>(null);
  comments: Record<string, string> = {};

  constructor(
    private adminService: AdminService,
    private disputeService: DisputeService,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.adminService.listOpenDisputes().subscribe({
      next: (disputes) => { this.disputes.set(disputes); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  decide(dispute: Dispute, decisionCode: string): void {
    const adminId = this.auth.currentUserId();
    if (!adminId) return;

    this.decidingId.set(dispute.id);
    this.errorMessage.set(null);
    this.disputeService.decide(dispute.id, adminId, decisionCode, this.comments[dispute.id]).subscribe({
      next: () => { this.decidingId.set(null); this.load(); },
      error: () => {
        this.decidingId.set(null);
        this.errorMessage.set('Impossible d\'enregistrer la décision.');
      }
    });
  }
}
