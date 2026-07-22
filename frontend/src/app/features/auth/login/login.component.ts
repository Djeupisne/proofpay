import { Component, computed, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

const PHONE_PATTERN = /^\+?[0-9]{8,15}$/;

type NotificationChannel = 'SMS' | 'EMAIL';

@Component({
  selector: 'pp-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent implements OnInit {
  // Champs utilisateur
  phone = signal('');
  code = signal('');
  email = signal('');
  channel = signal<NotificationChannel>('SMS');

  // États
  otpRequested = signal(false);
  requesting = signal(false);
  errorMessage = signal<string | null>(null);
  debugCode = signal<string | null>(null);

  // 🔥 Sauvegarder le choix de l'utilisateur
  rememberChannel = signal(true);

  normalizedPhone = computed(() => this.phone().trim().replace(/[\s\-().]/g, ''));
  isPhoneValid = computed(() => PHONE_PATTERN.test(this.normalizedPhone()));
  isEmailValid = computed(() => {
    const email = this.email().trim();
    return email === '' || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  });

  // 🔥 Validation du formulaire
  isFormValid = computed(() => {
    if (this.channel() === 'EMAIL') {
      return this.isPhoneValid() && this.isEmailValid() && this.email().trim() !== '';
    }
    return this.isPhoneValid();
  });

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit() {
    // 🔥 Restaurer le dernier canal choisi
    const savedChannel = localStorage.getItem('preferred_channel') as NotificationChannel;
    if (savedChannel && (savedChannel === 'SMS' || savedChannel === 'EMAIL')) {
      this.channel.set(savedChannel);
    }

    // 🔥 Restaurer l'email si sauvegardé
    const savedEmail = localStorage.getItem('preferred_email');
    if (savedEmail) {
      this.email.set(savedEmail);
    }
  }

  requestOtp(): void {
    this.errorMessage.set(null);
    
    if (!this.isPhoneValid()) {
      this.errorMessage.set('Numéro de téléphone invalide (8 à 15 chiffres, ex. +228 90 00 00 00).');
      return;
    }

    if (this.channel() === 'EMAIL' && !this.isEmailValid()) {
      this.errorMessage.set('Veuillez entrer une adresse email valide.');
      return;
    }

    // 🔥 Sauvegarder le choix de l'utilisateur
    if (this.rememberChannel()) {
      localStorage.setItem('preferred_channel', this.channel());
      if (this.email().trim()) {
        localStorage.setItem('preferred_email', this.email().trim());
      }
    }

    this.requesting.set(true);
    
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

  private extractMessage(err: HttpErrorResponse, fallback: string): string {
    return err?.error?.message ?? fallback;
  }
}
