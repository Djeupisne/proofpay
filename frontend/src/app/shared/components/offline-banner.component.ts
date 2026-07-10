import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

/** Signale une perte de connexion (§14 spécifications techniques : observabilité côté client
 * indirecte — l'utilisateur doit savoir si une action de paiement risque de ne pas aboutir). */
@Component({
  selector: 'pp-offline-banner',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (!online()) {
      <div class="pp-offline-banner">⚠ Pas de connexion internet — certaines actions peuvent échouer.</div>
    }
  `
})
export class OfflineBannerComponent {
  online = signal(navigator.onLine);

  constructor() {
    window.addEventListener('online', () => this.online.set(true));
    window.addEventListener('offline', () => this.online.set(false));
  }
}
