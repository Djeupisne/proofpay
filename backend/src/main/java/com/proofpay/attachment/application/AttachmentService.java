package com.proofpay.attachment.application;

import com.proofpay.attachment.domain.Attachment;
import com.proofpay.attachment.infrastructure.AttachmentRepository;
import com.proofpay.attachment.storage.StorageProvider;
import com.proofpay.common.exception.BusinessException;
import com.proofpay.dispute.application.DisputeService;
import com.proofpay.dispute.domain.Dispute;
import com.proofpay.transaction.application.TransactionService;
import com.proofpay.transaction.domain.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Ajout de preuves pour litige ou validation (règle métier #18).
 * Contrôle des types et de la taille max (§12 spécifications techniques).
 *
 * Contrôle d'accès (§11 spécifications fonctionnelles : "Les preuves et
 * commentaires ne doivent être visibles que par les parties concernées et le
 * support autorisé") : seuls l'acheteur, le vendeur de la transaction
 * concernée, ou un administrateur, peuvent lister/téléverser des pièces.
 */
@Service
public class AttachmentService {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "application/pdf");
    private static final Set<String> ALLOWED_OWNER_TYPES = Set.of("TRANSACTION", "DISPUTE");

    private final AttachmentRepository attachmentRepository;
    private final StorageProvider storageProvider;
    private final TransactionService transactionService;
    private final DisputeService disputeService;
    private final long maxAttachmentBytes;

    public AttachmentService(AttachmentRepository attachmentRepository,
                              StorageProvider storageProvider,
                              TransactionService transactionService,
                              DisputeService disputeService,
                              @Value("${proofpay.storage.max-attachment-mb}") long maxAttachmentMb) {
        this.attachmentRepository = attachmentRepository;
        this.storageProvider = storageProvider;
        this.transactionService = transactionService;
        this.disputeService = disputeService;
        this.maxAttachmentBytes = maxAttachmentMb * 1024 * 1024;
    }

    public Attachment upload(String ownerType, UUID ownerId, UUID uploadedBy, boolean isAdmin, MultipartFile file) {
        assertAccess(ownerType, ownerId, uploadedBy, isAdmin);

        if (!ALLOWED_MIME_TYPES.contains(file.getContentType())) {
            throw new BusinessException("INVALID_FILE_TYPE", "Type de fichier non autorisé");
        }
        if (file.getSize() > maxAttachmentBytes) {
            throw new BusinessException("FILE_TOO_LARGE", "Fichier trop volumineux");
        }
        try {
            String storagePath = storageProvider.store(file.getOriginalFilename(), file.getContentType(), file.getInputStream());
            Attachment attachment = Attachment.builder()
                    .ownerType(ownerType)
                    .ownerId(ownerId)
                    .originalName(file.getOriginalFilename())
                    .storedName(storagePath)
                    .mimeType(file.getContentType())
                    .sizeBytes(file.getSize())
                    .storagePath(storagePath)
                    .uploadedBy(uploadedBy)
                    .createdAt(Instant.now())
                    .build();
            return attachmentRepository.save(attachment);
        } catch (Exception e) {
            throw new BusinessException("UPLOAD_FAILED", "Échec du téléversement du fichier");
        }
    }

    public List<Attachment> listFor(String ownerType, UUID ownerId, UUID requesterId, boolean isAdmin) {
        assertAccess(ownerType, ownerId, requesterId, isAdmin);
        return attachmentRepository.findByOwnerTypeAndOwnerId(ownerType, ownerId);
    }

    /**
     * Résultat d'un téléchargement : le flux de données et les métadonnées
     * nécessaires pour construire la réponse HTTP (nom, type MIME, taille).
     */
    public record DownloadedFile(Attachment attachment, java.io.InputStream content) {}

    /**
     * Téléchargement d'une pièce jointe (§11 : "visibles uniquement par les
     * parties concernées et le support autorisé"). Le contrôle d'accès repose
     * sur le owner (transaction ou litige) de la pièce, jamais sur une donnée
     * fournie par le client : seul l'id de la pièce est pris en entrée.
     */
    public DownloadedFile download(UUID attachmentId, UUID requesterId, boolean isAdmin) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BusinessException("ATTACHMENT_NOT_FOUND", "Pièce jointe introuvable"));
        assertAccess(attachment.getOwnerType(), attachment.getOwnerId(), requesterId, isAdmin);
        try {
            return new DownloadedFile(attachment, storageProvider.retrieve(attachment.getStoragePath()));
        } catch (Exception e) {
            throw new BusinessException("DOWNLOAD_FAILED", "Échec de la lecture du fichier");
        }
    }

    /**
     * Résout la transaction concernée (directement, ou via le litige) et vérifie
     * que le demandeur en est l'acheteur ou le vendeur. Les admins passent
     * toujours, ce contrôle ne les concerne pas.
     */
    private void assertAccess(String ownerType, UUID ownerId, UUID requesterId, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        if (!ALLOWED_OWNER_TYPES.contains(ownerType)) {
            throw new BusinessException("INVALID_OWNER_TYPE", "Type de propriétaire de pièce jointe inconnu : " + ownerType);
        }

        Transaction tx = switch (ownerType) {
            case "TRANSACTION" -> transactionService.getOrThrow(ownerId);
            case "DISPUTE" -> {
                Dispute dispute = disputeService.getById(ownerId);
                yield transactionService.getOrThrow(dispute.getTransactionId());
            }
            default -> throw new BusinessException("INVALID_OWNER_TYPE", "Type de propriétaire inconnu : " + ownerType);
        };

        if (!requesterId.equals(tx.getBuyerId()) && !requesterId.equals(tx.getSellerId())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à accéder aux pièces jointes de cet élément");
        }
    }
}
