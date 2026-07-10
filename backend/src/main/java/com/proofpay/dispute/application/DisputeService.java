package com.proofpay.dispute.application;

import com.proofpay.common.audit.AuditLogger;
import com.proofpay.common.exception.BusinessException;
import com.proofpay.common.exception.ResourceNotFoundException;
import com.proofpay.dispute.domain.Dispute;
import com.proofpay.dispute.domain.DisputeStatus;
import com.proofpay.dispute.infrastructure.DisputeRepository;
import com.proofpay.notification.application.NotificationService;
import com.proofpay.notification.domain.NotificationChannel;
import com.proofpay.transaction.application.TransactionService;
import com.proofpay.transaction.application.TransactionStateMachine;
import com.proofpay.transaction.domain.Transaction;
import com.proofpay.transaction.domain.TransactionStatus;
import com.proofpay.user.application.ReputationService;
import com.proofpay.user.application.UserService;
import com.proofpay.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * UC-06/UC-07 : ouverture et arbitrage des litiges.
 * Règle métier #8/#9 : ouvrir un litige gèle toute évolution automatique.
 * Règle métier #10 : seul un administrateur peut décider (contrôlé par
 * SecurityConfig : POST /api/disputes/{id}/decision exige ROLE_ADMIN — voir
 * DisputeController).
 * Règle métier #29 : un seul litige actif à la fois.
 */
@Service
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final TransactionService transactionService;
    private final TransactionStateMachine stateMachine;
    private final AuditLogger auditLogger;
    private final ReputationService reputationService;
    private final NotificationService notificationService;
    private final UserService userService;

    public DisputeService(DisputeRepository disputeRepository,
                           TransactionService transactionService,
                           TransactionStateMachine stateMachine,
                           AuditLogger auditLogger,
                           ReputationService reputationService,
                           NotificationService notificationService,
                           UserService userService) {
        this.disputeRepository = disputeRepository;
        this.transactionService = transactionService;
        this.stateMachine = stateMachine;
        this.auditLogger = auditLogger;
        this.reputationService = reputationService;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @Transactional
    public Dispute open(UUID transactionId, UUID openedBy, String reasonCode, String reasonDetails) {
        Transaction tx = transactionService.getOrThrow(transactionId);
        stateMachine.assertDisputeOpenable(tx.getStatus());

        boolean hasActiveDispute = disputeRepository
                .findByTransactionIdAndStatusIn(transactionId, List.of(DisputeStatus.OPEN, DisputeStatus.UNDER_REVIEW))
                .isPresent();
        if (hasActiveDispute) {
            throw new BusinessException("DISPUTE_ALREADY_ACTIVE", "Un litige est déjà actif sur cette transaction");
        }

        TransactionStatus previousStatus = tx.getStatus();
        stateMachine.assertTransitionAllowed(previousStatus, TransactionStatus.LITIGE);
        tx.setStatus(TransactionStatus.LITIGE);

        Dispute dispute = Dispute.builder()
                .transactionId(transactionId)
                .openedBy(openedBy)
                .reasonCode(reasonCode)
                .reasonDetails(reasonDetails)
                .status(DisputeStatus.OPEN)
                .openedAt(Instant.now())
                .build();
        dispute = disputeRepository.save(dispute);

        auditLogger.logTransactionEvent(transactionId, "DISPUTE_OPENED",
                previousStatus.name(), TransactionStatus.LITIGE.name(), openedBy,
                Map.of("disputeId", dispute.getId(), "reasonCode", reasonCode));

        // Règle métier #21/#22 : trace le nombre de litiges ouverts par utilisateur.
        reputationService.recordDisputeOpened(openedBy);

        UUID otherParty = openedBy.equals(tx.getBuyerId()) ? tx.getSellerId() : tx.getBuyerId();
        notify(otherParty, transactionId, "DISPUTE_OPENED",
                "Un litige a été ouvert sur \"" + tx.getTitle() + "\". Motif : " + reasonCode);

        return dispute;
    }

    /** UC-07 : décision d'arbitrage, réservée à un administrateur (contrôlée au niveau API/sécurité). */
    @Transactional
    public Dispute decide(UUID disputeId, UUID adminId, String decisionCode, String comment) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Litige introuvable"));

        if (dispute.getStatus() == DisputeStatus.RESOLVED || dispute.getStatus() == DisputeStatus.REJECTED) {
            throw new BusinessException("DISPUTE_ALREADY_CLOSED", "Ce litige est déjà clôturé");
        }

        dispute.setStatus(DisputeStatus.RESOLVED);
        dispute.setDecisionCode(decisionCode);
        dispute.setDecisionComment(comment);
        dispute.setResolvedBy(adminId);
        dispute.setResolvedAt(Instant.now());
        disputeRepository.save(dispute);

        Transaction tx = transactionService.getOrThrow(dispute.getTransactionId());
        TransactionStatus targetStatus = switch (decisionCode) {
            case "REFUND_BUYER" -> TransactionStatus.REMBOURSEE;
            case "RELEASE_SELLER" -> TransactionStatus.TERMINEE;
            case "CANCEL" -> TransactionStatus.ANNULEE;
            default -> throw new BusinessException("UNKNOWN_DECISION", "Code de décision inconnu : " + decisionCode);
        };

        // La transition et sa journalisation sont déléguées à TransactionService,
        // seul propriétaire des mutations de statut (état centralisé). C'est aussi
        // ce hook qui déclenche la mise à jour de réputation en cas de TERMINEE.
        transactionService.applyAdminTransition(tx.getId(), targetStatus, adminId,
                "DISPUTE_DECIDED", Map.of("disputeId", dispute.getId(), "decisionCode", decisionCode));

        // Règle métier #21 : la partie perdante d'un litige voit sa réputation impactée.
        // REFUND_BUYER : le vendeur ne reçoit pas les fonds -> perdant.
        // RELEASE_SELLER : l'acheteur ne récupère pas les fonds -> perdant.
        // CANCEL : aucune partie n'est désignée perdante.
        if (decisionCode.equals("REFUND_BUYER")) {
            reputationService.recordDisputeLoss(tx.getSellerId());
        } else if (decisionCode.equals("RELEASE_SELLER")) {
            reputationService.recordDisputeLoss(tx.getBuyerId());
        }

        notify(tx.getBuyerId(), tx.getId(), "DISPUTE_DECIDED", "Décision du litige sur \"" + tx.getTitle() + "\" : " + decisionCode);
        notify(tx.getSellerId(), tx.getId(), "DISPUTE_DECIDED", "Décision du litige sur \"" + tx.getTitle() + "\" : " + decisionCode);

        return dispute;
    }

    /** Alimente l'écran admin de décision de litiges (liste des litiges à traiter, paginée). */
    public List<Dispute> listOpen(org.springframework.data.domain.Pageable pageable) {
        return disputeRepository.findByStatusInOrderByOpenedAtAsc(List.of(DisputeStatus.OPEN, DisputeStatus.UNDER_REVIEW), pageable);
    }

    public Dispute getById(UUID disputeId) {
        return disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Litige introuvable"));
    }

    private void notify(UUID userId, UUID transactionId, String templateCode, String message) {
        try {
            User user = userService.getById(userId);
            notificationService.notify(userId, transactionId, NotificationChannel.SMS, templateCode, user.getPhone(), message);
        } catch (Exception e) {
            // Règle métier #23 : une notification en échec ne doit jamais interrompre le flux métier.
        }
    }
}
