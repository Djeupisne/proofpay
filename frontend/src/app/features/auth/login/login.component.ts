import { Component, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

// Même règle que côté backend (PhoneNormalizer.isValid) : 8 à 15 chiffres,
// + optionnel. Dupliquée ici volontairement pour un retour immédiat côté
// utilisateur — la validation qui compte reste celle du serveur.
const PHONE_PATTERN = /^\+?[0-9]{8,15}$/;

/** Connexion par numéro de téléphone + OTP (§8.1 spécifications fonctionnelles). */
@Component({
  selector: 'pp-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  // ⚠️ Doivent être des signaux (pas de simples champs) : `computed()` ne se
  // recalcule que si les signaux qu'il lit changent. Avec un champ classique
  // lié par [(ngModel)], la frappe ne déclenche aucune réévaluation et la
  // validation reste figée sur sa toute première valeur (numéro vide, donc
  // invalide) — c'était le bug : le bouton restait grisé quel que soit le
  // numéro tapé.
  phone = signal('');
  code = signal('');

  otpRequested = signal(false);
  requesting = signal(false);
  errorMessage = signal<string | null>(null);
  debugCode = signal<string | null>(null); // affiché uniquement en mode démo/mock

  /** Numéro normalisé (espaces/tirets retirés) pour validation et affichage cohérents. */
  normalizedPhone = computed(() => this.phone().trim().replace(/[\s\-().]/g, ''));
  isPhoneValid = computed(() => PHONE_PATTERN.test(this.normalizedPhone()));

  constructor(private authService: AuthService, private router: Router) {}

  requestOtp(): void {
    this.errorMessage.set(null);
    if (!this.isPhoneValid()) {
      this.errorMessage.set('Numéro de téléphone invalide (8 à 15 chiffres, ex. +228 90 00 00 00).');
      return;
    }
    this.requesting.set(true);
    this.authService.requestOtp(this.normalizedPhone()).subscribe({
      next: (res: any) => {
        this.requesting.set(false);
        this.otpRequested.set(true);
        this.debugCode.set(res?.debugCode ?? null);
      },
      error: (err: HttpErrorResponse) => {
        this.requesting.set(false);
        this.errorMessage.set(this.extractMessage(err, "Impossible d'envoyer le code, réessayez."));
      }
    });
  }

  verifyOtp(): void {
    this.errorMessage.set(null);
    const code = this.code().trim();
    if (!code) {
      this.errorMessage.set('Le code reçu par SMS est obligatoire.');
      return;
    }
    this.authService.verifyOtp(this.normalizedPhone(), code).subscribe({
      next: () => this.router.navigate(['/transactions']),
      error: (err: HttpErrorResponse) => this.errorMessage.set(this.extractMessage(err, 'Code invalide ou expiré.'))
    });
  }

  /** Remonte le message métier renvoyé par le backend (cf. GlobalExceptionHandler) plutôt
   * qu'un texte générique, pour faciliter le diagnostic (ex: "OTP_INVALID" vs erreur réseau). */
  private extractMessage(err: HttpErrorResponse, fallback: string): string {
    return err?.error?.message ?? fallback;
  }
}
