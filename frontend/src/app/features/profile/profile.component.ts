import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../core/services/user.service';
import { User } from '../../core/models/user.model';

/** §8.1 : "Profil utilisateur avec nom, prénom, numéro, langue". */
@Component({
  selector: 'pp-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html'
})
export class ProfileComponent implements OnInit {
  user = signal<User | null>(null);
  loading = signal(true);
  saving = signal(false);
  successMessage = signal<string | null>(null);
  errorMessage = signal<string | null>(null);

  firstName = '';
  lastName = '';
  email = '';
  preferredLanguage = 'fr';

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.userService.me().subscribe({
      next: (u) => {
        this.user.set(u);
        this.firstName = u.firstName ?? '';
        this.lastName = u.lastName ?? '';
        this.email = u.email ?? '';
        this.preferredLanguage = u.preferredLanguage ?? 'fr';
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  save(): void {
    this.saving.set(true);
    this.successMessage.set(null);
    this.errorMessage.set(null);
    this.userService.updateMe({
      firstName: this.firstName,
      lastName: this.lastName,
      email: this.email,
      preferredLanguage: this.preferredLanguage
    }).subscribe({
      next: (u) => {
        this.user.set(u);
        this.saving.set(false);
        this.successMessage.set('Profil mis à jour.');
      },
      error: () => {
        this.saving.set(false);
        this.errorMessage.set('Impossible de mettre à jour le profil.');
      }
    });
  }
}
