package com.proofpay.attachment.api;

import com.proofpay.attachment.application.AttachmentService;
import com.proofpay.attachment.domain.Attachment;
import com.proofpay.security.util.SecurityUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public Attachment upload(@RequestParam String ownerType,
                              @RequestParam UUID ownerId,
                              @RequestParam("file") MultipartFile file) {
        UUID uploadedBy = SecurityUtils.currentUserId();
        boolean isAdmin = SecurityUtils.isAdmin();
        
        return attachmentService.upload(ownerType, ownerId, uploadedBy, isAdmin, file);
    }

    @GetMapping
    public List<Attachment> list(@RequestParam String ownerType, @RequestParam UUID ownerId) {
        UUID requesterId = SecurityUtils.currentUserId();
        boolean isAdmin = SecurityUtils.isAdmin();
        
        return attachmentService.listFor(ownerType, ownerId, requesterId, isAdmin);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable UUID id) {
        UUID requesterId = SecurityUtils.currentUserId();
        boolean isAdmin = SecurityUtils.isAdmin();
        
        AttachmentService.DownloadedFile file = attachmentService.download(id, requesterId, isAdmin);
        Attachment attachment = file.attachment();

        String safeFilename = ContentDisposition.attachment()
                .filename(attachment.getOriginalName(), StandardCharsets.UTF_8)
                .build()
                .toString();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, safeFilename)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(attachment.getSizeBytes()))
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=0, no-cache")
                .body(new InputStreamResource(file.content()));
    }
}
