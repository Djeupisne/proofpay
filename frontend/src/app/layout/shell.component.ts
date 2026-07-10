import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationStart, Router, RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth/auth.service';
import { OfflineBannerComponent } from '../shared/components/offline-banner.component';

/** Coquille de l'application : barre de navigation (menu hamburger sur mobile) + contenu. */
@Component({
  selector: 'pp-shell',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterOutlet, OfflineBannerComponent],
  templateUrl: './shell.component.html'
})
export class ShellComponent {
  menuOpen = signal(false);

  constructor(public auth: AuthService, private router: Router) {
    // Referme le menu mobile dès qu'on navigue (sinon il reste ouvert par-dessus la page suivante).
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationStart) {
        this.menuOpen.set(false);
      }
    });
  }

  toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }

  logout(): void {
    this.menuOpen.set(false);
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
