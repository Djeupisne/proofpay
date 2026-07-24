package com.proofpay.attachment.api;

import com.proofpay.attachment.application.AttachmentService;
import com.proofpay.attachment.domain.Attachment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Attachment> uploadAttachment(
            @RequestParam("ownerType") String ownerType,
            @RequestParam("ownerId") UUID ownerId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploadedBy") UUID uploadedBy) throws Exception {
        
        Attachment attachment = attachmentService.uploadAttachment(ownerType, ownerId, file, uploadedBy);
        return ResponseEntity.ok(attachment);
    }

    @GetMapping("/owner/{ownerType}/{ownerId}")
    public ResponseEntity<List<Attachment>> getAttachmentsByOwner(
            @PathVariable String ownerType,
            @PathVariable UUID ownerId) {
        
        List<Attachment> attachments = attachmentService.getAttachmentsByOwner(ownerType, ownerId);
        return ResponseEntity.ok(attachments);
    }

    @GetMapping("/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable UUID attachmentId) throws Exception {
        InputStream inputStream = attachmentService.downloadAttachment(attachmentId);
        Attachment attachment = attachmentService.getAttachment(attachmentId);
        
        InputStreamResource resource = new InputStreamResource(inputStream);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getOriginalName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable UUID attachmentId) throws Exception {
        attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.noContent().build();
    }
}
