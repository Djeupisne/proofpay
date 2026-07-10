import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Attachment } from '../models/attachment.model';

@Injectable({ providedIn: 'root' })
export class AttachmentService {
  private readonly baseUrl = `${environment.apiUrl}/attachments`;

  constructor(private http: HttpClient) {}

  list(ownerType: 'TRANSACTION' | 'DISPUTE', ownerId: string): Observable<Attachment[]> {
    return this.http.get<Attachment[]>(this.baseUrl, { params: { ownerType, ownerId } });
  }

  upload(ownerType: 'TRANSACTION' | 'DISPUTE', ownerId: string, file: File): Observable<Attachment> {
    const formData = new FormData();
    formData.append('ownerType', ownerType);
    formData.append('ownerId', ownerId);
    formData.append('file', file);
    return this.http.post<Attachment>(this.baseUrl, formData);
  }

  /** Récupère le fichier en blob et déclenche son téléchargement dans le navigateur. */
  download(attachment: Attachment): void {
    this.http.get(`${this.baseUrl}/${attachment.id}/download`, { responseType: 'blob' }).subscribe((blob) => {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = attachment.originalName;
      link.click();
      window.URL.revokeObjectURL(url);
    });
  }
}
