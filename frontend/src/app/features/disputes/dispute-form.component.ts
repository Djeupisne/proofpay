import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DisputeService } from '../../core/services/dispute.service';
import { AuthService } from '../../core/auth/auth.service';

/** UC-06 : ouvrir un litige (§8.6 spécifications fonctionnelles). */
@Component({
  selector: 'pp-dispute-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dispute-form.component.html'
})
export class DisputeFormComponent {
  reasonCode = 'NON_LIVRE';
  reasonDetails = '';
  submitting = signal(false);
  errorMessage = signal<string | null>(null);

  private transactionId: string;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private disputeService: DisputeService,
    private auth: AuthService
  ) {
    this.transactionId = this.route.snapshot.paramMap.get('id')!;
  }

  submit(): void {
    const userId = this.auth.currentUserId();
    if (!userId) return;

    this.submitting.set(true);
    this.disputeService.open(this.transactionId, userId, this.reasonCode, this.reasonDetails).subscribe({
      next: () => this.router.navigate(['/transactions', this.transactionId]),
      error: () => {
        this.errorMessage.set("Impossible d'ouvrir le litige.");
        this.submitting.set(false);
      }
    });
  }
}
