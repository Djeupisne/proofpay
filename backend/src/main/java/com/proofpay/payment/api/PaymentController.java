package com.proofpay.payment.api;

import com.proofpay.payment.application.PaymentService;
import com.proofpay.payment.domain.Payment;
import com.proofpay.transaction.application.TransactionService;
import com.proofpay.transaction.domain.Transaction;
import com.proofpay.transaction.domain.TransactionStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class PaymentController {

    private final PaymentService paymentService;
    private final TransactionService transactionService;

    public PaymentController(PaymentService paymentService, TransactionService transactionService) {
        this.paymentService = paymentService;
        this.transactionService = transactionService;
    }

    /**
     * Initier un paiement
     * POST /api/transactions/{id}/pay
     */
    @PostMapping("/{id}/pay")
    public ResponseEntity<Map<String, Object>> pay(@PathVariable UUID id, @Valid @RequestBody PayRequest request) {
        // 1. Vérifier la transaction
        Transaction tx = transactionService.getOrThrow(id);

        // 2. Vérifier le statut
        // ⚠️ CORRECTIF : tx.getStatus() renvoie un TransactionStatus (enum), jamais un
        // String. "EN_ATTENTE_PAIEMENT".equals(enumValue) est donc TOUJOURS false,
        // ce qui faisait échouer 100% des paiements avant ce correctif.
        if (tx.getStatus() != TransactionStatus.EN_ATTENTE_PAIEMENT) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "La transaction n'est pas en attente de paiement");
            error.put("status", tx.getStatus());
            return ResponseEntity.badRequest().body(error);
        }

        // 3. Initier le paiement
        Payment payment = paymentService.initiate(id, request.payerPhone());

        // 4. Marquer comme payée (transition d'état + audit + notifications)
        transactionService.markPaid(id);

        // 5. Construire la réponse
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Paiement effectué avec succès");
        response.put("paymentId", payment.getId());
        response.put("transactionId", tx.getId());
        response.put("amount", tx.getAmount());
        response.put("status", "CONFIRMED");
        response.put("paidAt", Instant.now());

        // 6. URL de redirection (pour le frontend)
        response.put("redirectUrl", "/transactions/" + tx.getId() + "/success");

        return ResponseEntity.ok(response);
    }

    /**
     * Vérifier le statut d'un paiement
     * GET /api/transactions/{id}/payment-status
     */
    @GetMapping("/{id}/payment-status")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable UUID id) {
        Transaction tx = transactionService.getOrThrow(id);
        TransactionStatus status = tx.getStatus();

        Map<String, Object> response = new HashMap<>();
        response.put("transactionId", tx.getId());
        response.put("status", status);
        // ⚠️ Même correctif : comparaison enum-à-enum au lieu de String.equals(enum).
        response.put("isPaid", status == TransactionStatus.PAYE
                || status == TransactionStatus.EN_LIVRAISON
                || status == TransactionStatus.A_CONFIRMER);

        return ResponseEntity.ok(response);
    }

    public record PayRequest(@NotBlank(message = "Le numéro de paiement est obligatoire") String payerPhone) {}
}