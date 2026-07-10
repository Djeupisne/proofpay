import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { AdminSetting } from '../../../core/models/admin-setting.model';

/**
 * Gestion des paramètres métier : frais, délais de confirmation (§8.8).
 * Les valeurs sont lues/écrites directement dans admin_settings côté backend
 * (AdminSettingsService), donc une modification ici est effective
 * immédiatement, sans redéploiement.
 */
@Component({
  selector: 'pp-admin-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-settings.component.html'
})
export class AdminSettingsComponent implements OnInit {
  settings = signal<AdminSetting[]>([]);
  loading = signal(true);
  savingKey = signal<string | null>(null);
  successMessage = signal<string | null>(null);
  errorMessage = signal<string | null>(null);
  editValues: Record<string, string> = {};

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.adminService.listSettings().subscribe({
      next: (settings) => {
        this.settings.set(settings);
        settings.forEach(s => this.editValues[s.settingKey] = s.settingValue);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  save(setting: AdminSetting): void {
    const newValue = this.editValues[setting.settingKey];
    this.savingKey.set(setting.settingKey);
    this.successMessage.set(null);
    this.errorMessage.set(null);

    this.adminService.updateSetting(setting.settingKey, newValue).subscribe({
      next: () => {
        this.savingKey.set(null);
        this.successMessage.set(`${setting.settingKey} mis à jour.`);
        this.load();
      },
      error: () => {
        this.savingKey.set(null);
        this.errorMessage.set('Échec de la mise à jour.');
      }
    });
  }
}
