package com.proofpay.dispute.api;

import com.proofpay.dispute.application.DisputeService;
import com.proofpay.dispute.domain.Dispute;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class DisputeController {

    private final DisputeService disputeService;

    public DisputeController(DisputeService disputeService) {
        this.disputeService = disputeService;
    }

    @PostMapping("/transactions/{id}/open-dispute")
    public ResponseEntity<Dispute> open(@PathVariable("id") UUID transactionId,
                                         @Valid @RequestBody OpenDisputeRequest request) {
        return ResponseEntity.ok(disputeService.open(
                transactionId, request.openedBy(), request.reasonCode(), request.reasonDetails()));
    }

    @GetMapping("/disputes/{id}")
    public Dispute get(@PathVariable("id") UUID disputeId) {
        return disputeService.getById(disputeId);
    }

    /** Réservé au rôle ADMIN (cf. SecurityConfig : POST /api/disputes/{id}/decision exige ROLE_ADMIN). */
    @PostMapping("/disputes/{id}/decision")
    public ResponseEntity<Dispute> decide(@PathVariable("id") UUID disputeId,
                                           @Valid @RequestBody DecisionRequest request) {
        return ResponseEntity.ok(disputeService.decide(
                disputeId, request.adminId(), request.decisionCode(), request.comment()));
    }

    public record OpenDisputeRequest(@NotNull UUID openedBy, @NotBlank String reasonCode, String reasonDetails) {
    }

    public record DecisionRequest(@NotNull UUID adminId, @NotBlank String decisionCode, String comment) {
    }
}
