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

// 🔥 NOUVEAU : Interface pour le canal
type NotificationChannel = 'SMS' | 'EMAIL';

/** Connexion par numéro de téléphone + OTP (§8.1 spécifications fonctionnelles). */
@Component({
  selector: 'pp-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  // Champs utilisateur
  phone = signal('');
  code = signal('');
  email = signal(''); // 🔥 NOUVEAU : Email pour le canal EMAIL
  channel = signal<NotificationChannel>('SMS'); // 🔥 NOUVEAU : Canal choisi

  // États
  otpRequested = signal(false);
  requesting = signal(false);
  errorMessage = signal<string | null>(null);
  debugCode = signal<string | null>(null);

  /** Numéro normalisé (espaces/tirets retirés) pour validation et affichage cohérents. */
  normalizedPhone = computed(() => this.phone().trim().replace(/[\s\-().]/g, ''));
  isPhoneValid = computed(() => PHONE_PATTERN.test(this.normalizedPhone()));

  // 🔥 NOUVEAU : Validation de l'email (simple)
  isEmailValid = computed(() => {
    const email = this.email().trim();
    return email === '' || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  });

  // 🔥 NOUVEAU : Le formulaire est-il valide ?
  isFormValid = computed(() => {
    if (this.channel() === 'EMAIL') {
      return this.isPhoneValid() && this.isEmailValid() && this.email().trim() !== '';
    }
    return this.isPhoneValid();
  });

  constructor(private authService: AuthService, private router: Router) {}

  requestOtp(): void {
    this.errorMessage.set(null);
    
    // Validation
    if (!this.isPhoneValid()) {
      this.errorMessage.set('Numéro de téléphone invalide (8 à 15 chiffres, ex. +228 90 00 00 00).');
      return;
    }

    // Si canal EMAIL, vérifier l'email
    if (this.channel() === 'EMAIL') {
      if (!this.isEmailValid() || !this.email().trim()) {
        this.errorMessage.set('Veuillez entrer une adresse email valide.');
        return;
      }
    }

    this.requesting.set(true);
    
    // 🔥 MODIFICATION : Appel avec email et canal
    this.authService.requestOtp(
      this.normalizedPhone(),
      this.channel() === 'EMAIL' ? this.email().trim() : undefined,
      this.channel()
    ).subscribe({
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
      this.errorMessage.set('Le code reçu est obligatoire.');
      return;
    }
    this.authService.verifyOtp(this.normalizedPhone(), code).subscribe({
      next: () => this.router.navigate(['/transactions']),
      error: (err: HttpErrorResponse) => this.errorMessage.set(this.extractMessage(err, 'Code invalide ou expiré.'))
    });
  }

  /** Remonte le message métier renvoyé par le backend. */
  private extractMessage(err: HttpErrorResponse, fallback: string): string {
    return err?.error?.message ?? fallback;
  }
}
