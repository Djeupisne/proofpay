import { Component, Input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AttachmentService } from '../../core/services/attachment.service';
import { Attachment } from '../../core/models/attachment.model';

/**
 * Preuves/pièces jointes attachées à une transaction ou un litige
 * (règle métier #18, §11 : visibles uniquement par les parties concernées).
 * L'accès réel est vérifié côté backend ; ce composant se contente d'afficher
 * ce que l'API accepte de renvoyer pour l'utilisateur courant.
 */
@Component({
  selector: 'pp-attachment-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="pp-attachments">
      <div class="pp-attachments-header">
        <h4>Pièces jointes</h4>
        <label class="pp-attachments-upload" [class.pp-attachments-upload--busy]="uploading()">
          @if (uploading()) {
            <span class="spinner"></span> Envoi...
          } @else {
            + Ajouter un fichier
          }
          <input type="file" accept="image/jpeg,image/png,image/webp,application/pdf"
                 (change)="onFileSelected($event)" [disabled]="uploading()" hidden>
        </label>
      </div>

      @if (errorMessage()) {
        <p class="pp-error">{{ errorMessage() }}</p>
      }

      @if (loading()) {
        <p class="pp-hint">Chargement des pièces jointes...</p>
      } @else if (attachments().length === 0) {
        <p class="pp-hint">Aucune pièce jointe pour le moment.</p>
      } @else {
        <ul class="pp-attachments-list">
          @for (att of attachments(); track att.id) {
            <li class="pp-attachment-item">
              <span class="pp-attachment-icon">{{ iconFor(att.mimeType) }}</span>
              <div class="pp-attachment-meta">
                <span class="pp-attachment-name">{{ att.originalName }}</span>
                <span class="pp-attachment-sub">{{ formatSize(att.sizeBytes) }} · {{ att.createdAt | date:'dd/MM/yyyy HH:mm' }}</span>
              </div>
              <button type="button" class="pp-attachment-download" (click)="attachmentService.download(att)">Télécharger</button>
            </li>
          }
        </ul>
      }
    </div>
  `
})
export class AttachmentListComponent implements OnInit {
  @Input({ required: true }) ownerType!: 'TRANSACTION' | 'DISPUTE';
  @Input({ required: true }) ownerId!: string;

  attachments = signal<Attachment[]>([]);
  loading = signal(true);
  uploading = signal(false);
  errorMessage = signal<string | null>(null);

  constructor(public attachmentService: AttachmentService) {}

  ngOnInit(): void {
    this.reload();
  }

  private reload(): void {
    this.loading.set(true);
    this.attachmentService.list(this.ownerType, this.ownerId).subscribe({
      next: (list) => { this.attachments.set(list); this.loading.set(false); },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set("Impossible de charger les pièces jointes.");
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.errorMessage.set(null);
    this.uploading.set(true);
    this.attachmentService.upload(this.ownerType, this.ownerId, file).subscribe({
      next: () => {
        this.uploading.set(false);
        input.value = '';
        this.reload();
      },
      error: (err) => {
        this.uploading.set(false);
        input.value = '';
        this.errorMessage.set(err?.error?.message || "Échec de l'envoi. Vérifiez le type et la taille du fichier.");
      }
    });
  }

  iconFor(mimeType: string): string {
    if (mimeType === 'application/pdf') return '📄';
    return '🖼️';
  }

  formatSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} o`;
    if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)} Ko`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} Mo`;
  }
}
