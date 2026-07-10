package com.proofpay.attachment.infrastructure;

import com.proofpay.attachment.domain.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    List<Attachment> findByOwnerTypeAndOwnerId(String ownerType, UUID ownerId);
}
