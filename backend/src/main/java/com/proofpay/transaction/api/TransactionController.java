package com.proofpay.transaction.api;

import com.proofpay.transaction.application.TransactionService;
import com.proofpay.transaction.domain.ConfirmationMode;
import com.proofpay.transaction.domain.Transaction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    // ⚠️ SUPPRIMEZ PaymentService du constructeur
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public org.springframework.data.domain.Page<Transaction> listMine(@RequestParam UUID userId,
                                      @PageableDefault(size = 20) org.springframework.data.domain.Pageable pageable) {
        return transactionService.listForUser(userId, pageable);
    }

    @PostMapping
    public ResponseEntity<Transaction> create(@Valid @RequestBody CreateTransactionRequest request) {
        Transaction tx = transactionService.create(
                request.buyerId(), request.sellerPhone(), request.title(),
                request.description(), request.categoryCode(), request.amount(),
                request.confirmationMode(),
                request.confirmationSecret(), request.deliveryDelayHours());
        return ResponseEntity.ok(tx);
    }

    @GetMapping("/{id}")
    public Transaction get(@PathVariable UUID id) {
        return transactionService.getOrThrow(id);
    }

    @PostMapping("/{id}/accept")
    public Transaction accept(@PathVariable UUID id, @RequestParam UUID sellerId) {
        return transactionService.accept(id, sellerId);
    }

    @PostMapping("/{id}/reject")
    public Transaction reject(@PathVariable UUID id, @RequestParam UUID sellerId,
                              @RequestBody(required = false) RejectRequest body) {
        String reason = body != null ? body.reason() : null;
        return transactionService.reject(id, sellerId, reason);
    }

    @PostMapping("/{id}/mark-delivered")
    public Transaction markDelivered(@PathVariable UUID id, @RequestParam UUID sellerId) {
        return transactionService.markDelivered(id, sellerId);
    }

    @PostMapping("/{id}/confirm")
    public Transaction confirm(@PathVariable UUID id, @RequestParam UUID buyerId,
                               @RequestBody(required = false) ConfirmRequest body) {
        String code = body != null ? body.code() : null;
        return transactionService.confirm(id, buyerId, code);
    }

    // ⚠️ SUPPRIMEZ LA MÉTHODE pay() ICI
    // Elle est déjà dans PaymentController

    public record CreateTransactionRequest(
            @NotNull UUID buyerId,
            @NotBlank String sellerPhone,
            @NotBlank String title,
            String description,
            String categoryCode,
            @NotNull @Positive BigDecimal amount,
            @NotNull ConfirmationMode confirmationMode,
            String confirmationSecret,
            Integer deliveryDelayHours
    ) {}

    public record RejectRequest(String reason) {}

    public record ConfirmRequest(String code) {}
}