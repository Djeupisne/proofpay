import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

/** Badge coloré réutilisable pour l'affichage du statut d'une transaction. */
@Component({
  selector: 'pp-status-badge',
  standalone: true,
  imports: [CommonModule],
  template: `<span class="pp-badge" [class]="'pp-badge--' + status.toLowerCase()">{{ status }}</span>`
})
export class StatusBadgeComponent {
  @Input({ required: true }) status!: string;
}
