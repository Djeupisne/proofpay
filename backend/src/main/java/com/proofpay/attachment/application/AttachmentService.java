package com.proofpay.attachment.application;

import com.proofpay.attachment.domain.Attachment;
import com.proofpay.attachment.infrastructure.AttachmentRepository;
import com.proofpay.attachment.storage.StorageProvider;
import com.proofpay.dispute.domain.Dispute;
import com.proofpay.dispute.infrastructure.DisputeRepository;
import com.proofpay.transaction.domain.Transaction;
import com.proofpay.transaction.infrastructure.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final StorageProvider storageProvider;
    private final TransactionRepository transactionRepository;
    private final DisputeRepository disputeRepository;

    public AttachmentService(AttachmentRepository attachmentRepository,
                             StorageProvider storageProvider,
                             TransactionRepository transactionRepository,
                             DisputeRepository disputeRepository) {
        this.attachmentRepository = attachmentRepository;
        this.storageProvider = storageProvider;
        this.transactionRepository = transactionRepository;
        this.disputeRepository = disputeRepository;
    }

    public Attachment uploadAttachment(String ownerType, UUID ownerId, MultipartFile file, UUID uploadedBy) throws Exception {
        // Vérifier que l'entité existe
        if ("TRANSACTION".equals(ownerType)) {
            Transaction transaction = transactionRepository.findById(ownerId)
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));
        } else if ("DISPUTE".equals(ownerType)) {
            Dispute dispute = disputeRepository.findById(ownerId)
                    .orElseThrow(() -> new RuntimeException("Dispute not found"));
        }

        // Stocker le fichier
        String storagePath = storageProvider.store(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getInputStream()
        );

        // Créer l'attachement
        Attachment attachment = Attachment.builder()
                .ownerType(ownerType)
                .ownerId(ownerId)
                .originalName(file.getOriginalFilename())
                .storedName(storagePath.substring(storagePath.lastIndexOf("/") + 1))
                .mimeType(file.getContentType())
                .sizeBytes(file.getSize())
                .storagePath(storagePath)
                .uploadedBy(uploadedBy)
                .createdAt(Instant.now())
                .build();

        return attachmentRepository.save(attachment);
    }

    public List<Attachment> getAttachmentsByOwner(String ownerType, UUID ownerId) {
        return attachmentRepository.findByOwnerTypeAndOwnerId(ownerType, ownerId);
    }

    public Attachment getAttachment(UUID attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
    }

    public void deleteAttachment(UUID attachmentId) throws Exception {
        Attachment attachment = getAttachment(attachmentId);
        storageProvider.delete(attachment.getStoragePath());
        attachmentRepository.delete(attachment);
    }

    public InputStream downloadAttachment(UUID attachmentId) throws Exception {
        Attachment attachment = getAttachment(attachmentId);
        return storageProvider.retrieve(attachment.getStoragePath());
    }
}
