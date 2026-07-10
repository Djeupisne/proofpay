package com.proofpay.transaction.application;

import com.proofpay.admin.application.AdminSettingsService;
import com.proofpay.common.audit.AuditLogger;
import com.proofpay.common.exception.BusinessException;
import com.proofpay.common.exception.ResourceNotFoundException;
import com.proofpay.common.util.ReferenceGenerator;
import com.proofpay.notification.application.NotificationService;
import com.proofpay.notification.domain.NotificationChannel;
import com.proofpay.security.otp.OtpService;
import com.proofpay.transaction.domain.ConfirmationMode;
import com.proofpay.transaction.domain.Transaction;
import com.proofpay.transaction.domain.TransactionStatus;
import com.proofpay.transaction.infrastructure.TransactionRepository;
import com.proofpay.user.application.ReputationService;
import com.proofpay.user.application.UserService;
import com.proofpay.user.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Cas d'usage du cœur transactionnel : création, acceptation, refus, etc.
 * Toute mutation de statut passe par TransactionStateMachine + AuditLogger,
 * jamais de changement de statut "à la main" ailleurs dans le code.
 */
@Service
public class TransactionService {

    private static final String NOTIF_TX_CREATED = "TX_CREATED";
    private static final String NOTIF_TX_ACCEPTED = "TX_ACCEPTED";
    private static final String NOTIF_TX_REJECTED = "TX_REJECTED";
    private static final String NOTIF_PAYMENT_CONFIRMED = "PAYMENT_CONFIRMED";
    private static final String NOTIF_AWAITING_CONFIRMATION = "AWAITING_CONFIRMATION";
    private static final String NOTIF_TX_COMPLETED = "TX_COMPLETED";

    private final TransactionRepository transactionRepository;
    private final TransactionStateMachine stateMachine;
    private final FeeCalculator feeCalculator;
    private final AuditLogger auditLogger;
    private final UserService userService;
    private final ReputationService reputationService;
    private final NotificationService notificationService;
    private final AdminSettingsService adminSettingsService;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final long defaultConfirmationDelayHoursFallback;

    public TransactionService(TransactionRepository transactionRepository,
                              TransactionStateMachine stateMachine,
                              FeeCalculator feeCalculator,
                              AuditLogger auditLogger,
                              UserService userService,
                              ReputationService reputationService,
                              NotificationService notificationService,
                              AdminSettingsService adminSettingsService,
                              OtpService otpService,
                              PasswordEncoder passwordEncoder,
                              @Value("${proofpay.transaction.default-confirmation-delay-hours}") long defaultConfirmationDelayHoursFallback) {
        this.transactionRepository = transactionRepository;
        this.stateMachine = stateMachine;
        this.feeCalculator = feeCalculator;
        this.auditLogger = auditLogger;
        this.userService = userService;
        this.reputationService = reputationService;
        this.notificationService = notificationService;
        this.adminSettingsService = adminSettingsService;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
        this.defaultConfirmationDelayHoursFallback = defaultConfirmationDelayHoursFallback;
    }

    /**
     * UC-01 : créer une transaction (règle métier #30 : montant positif).
     * @param sellerPhone identifiant du vendeur par numéro de téléphone (§8.2) ; doit déjà
     *                    correspondre à un compte ProofPay existant.
     * @param confirmationSecret requis si confirmationMode == CODE_SECRET (§8.5) ; ignoré sinon.
     * @param deliveryDelayHours délai de livraison/confirmation propre à cette transaction
     *                           (§19 : délai configurable) ; si null, la valeur par défaut des
     *                           paramètres admin (DEFAULT_CONFIRMATION_DELAY_HOURS) est utilisée.
     */
    @Transactional
    public Transaction create(UUID buyerId, String sellerPhone, String title, String description,
                              String categoryCode, BigDecimal amount, ConfirmationMode confirmationMode,
                              String confirmationSecret, Integer deliveryDelayHours) {
        User buyer = userService.getById(buyerId);
        if (!buyer.canTransact()) {
            throw new BusinessException("USER_SUSPENDED", "Compte suspendu, création impossible");
        }
        User seller = userService.getByPhone(sellerPhone);
        if (seller.getId().equals(buyerId)) {
            throw new BusinessException("INVALID_SELLER", "Vous ne pouvez pas créer une transaction avec vous-même");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "Le montant doit être positif");
        }
        if (confirmationMode == ConfirmationMode.CODE_SECRET
                && (confirmationSecret == null || confirmationSecret.isBlank())) {
            throw new BusinessException("SECRET_REQUIRED", "Un code secret est requis pour ce mode de confirmation");
        }

        BigDecimal fees = feeCalculator.computeFees(amount);
        long delayHours = deliveryDelayHours != null && deliveryDelayHours > 0
                ? deliveryDelayHours
                : adminSettingsService.getLong("DEFAULT_CONFIRMATION_DELAY_HOURS", defaultConfirmationDelayHoursFallback);

        Transaction tx = Transaction.builder()
                .publicRef(ReferenceGenerator.generateTransactionRef())
                .buyerId(buyerId)
                .sellerId(seller.getId())
                .title(title)
                .description(description)
                .categoryCode(categoryCode)
                .currency("XOF")
                .amount(amount)
                .fees(fees)
                .status(TransactionStatus.BROUILLON)
                .confirmationMode(confirmationMode)
                .confirmationSecretHash(confirmationMode == ConfirmationMode.CODE_SECRET
                        ? passwordEncoder.encode(confirmationSecret) : null)
                .deliveryDeadline(Instant.now().plusSeconds(delayHours * 3600))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        tx = transactionRepository.save(tx);

        transitionTo(tx, TransactionStatus.EN_ATTENTE_ACCEPTATION, buyerId, "TX_SENT_TO_SELLER", Map.of());
        tx = transactionRepository.save(tx);

        // 🔥 FORCER LE FLUSH POUR RENDRE LA TRANSACTION DISPONIBLE EN BASE
        // La notification est asynchrone (@Async) et s'exécute dans un autre thread
        // Sans flush, la transaction peut ne pas être encore présente en base
        transactionRepository.flush();

        notify(tx.getSellerId(), tx.getId(), NOTIF_TX_CREATED,
                "Nouvelle demande de transaction \"" + tx.getTitle() + "\" - " + tx.getAmount() + " " + tx.getCurrency());
        return tx;
    }

    /** Historique des transactions (achats + ventes) de l'utilisateur connecté (§13 : paginé). */
    public org.springframework.data.domain.Page<Transaction> listForUser(UUID userId, org.springframework.data.domain.Pageable pageable) {
        return transactionRepository.findByBuyerIdOrSellerIdOrderByCreatedAtDesc(userId, userId, pageable);
    }

    /** UC-02 : le vendeur accepte. */
    @Transactional
    public Transaction accept(UUID transactionId, UUID sellerId) {
        Transaction tx = getOrThrow(transactionId);
        assertActor(tx.getSellerId(), sellerId);
        User seller = userService.getById(sellerId);
        if (!seller.canTransact()) {
            // Règle métier #14 : un utilisateur suspendu ne peut plus accepter de transactions.
            throw new BusinessException("USER_SUSPENDED", "Compte suspendu, acceptation impossible");
        }
        transitionTo(tx, TransactionStatus.EN_ATTENTE_PAIEMENT, sellerId, "TX_ACCEPTED", Map.of());
        tx = transactionRepository.save(tx);

        // 🔥 FORCER LE FLUSH AVANT LA NOTIFICATION ASYNCHRONE
        transactionRepository.flush();

        notify(tx.getBuyerId(), tx.getId(), NOTIF_TX_ACCEPTED,
                "Le vendeur a accepté \"" + tx.getTitle() + "\". Vous pouvez procéder au paiement.");
        return tx;
    }

    /** UC-02 : le vendeur refuse. */
    @Transactional
    public Transaction reject(UUID transactionId, UUID sellerId, String reason) {
        Transaction tx = getOrThrow(transactionId);
        assertActor(tx.getSellerId(), sellerId);
        transitionTo(tx, TransactionStatus.REFUSEE, sellerId, "TX_REJECTED", Map.of("reason", reason));
        tx = transactionRepository.save(tx);

        // 🔥 FORCER LE FLUSH AVANT LA NOTIFICATION ASYNCHRONE
        transactionRepository.flush();

        notify(tx.getBuyerId(), tx.getId(), NOTIF_TX_REJECTED,
                "Le vendeur a refusé \"" + tx.getTitle() + "\"" + (reason != null ? " : " + reason : "."));
        return tx;
    }

    /** Appelé par PaymentService une fois le paiement confirmé (règle métier #2, #3, #25). */
    @Transactional
    public Transaction markPaid(UUID transactionId) {
        Transaction tx = getOrThrow(transactionId);
        if (tx.getStatus() != TransactionStatus.EN_ATTENTE_PAIEMENT) {
            throw new BusinessException("PAY_NOT_ALLOWED", "Le paiement n'est possible qu'en statut EN_ATTENTE_PAIEMENT");
        }
        tx.setPaidAt(Instant.now());
        transitionTo(tx, TransactionStatus.PAYE, null, "PAYMENT_CONFIRMED", Map.of());
        tx = transactionRepository.save(tx);

        // 🔥 FORCER LE FLUSH AVANT LES NOTIFICATIONS ASYNCHRONES
        transactionRepository.flush();

        notify(tx.getBuyerId(), tx.getId(), NOTIF_PAYMENT_CONFIRMED,
                "Paiement confirmé pour \"" + tx.getTitle() + "\". Les fonds sont bloqués jusqu'à livraison.");
        notify(tx.getSellerId(), tx.getId(), NOTIF_PAYMENT_CONFIRMED,
                "Paiement reçu pour \"" + tx.getTitle() + "\". Vous pouvez procéder à la livraison.");
        return tx;
    }

    /** UC-04 : le vendeur déclare la livraison. */
    @Transactional
    public Transaction markDelivered(UUID transactionId, UUID sellerId) {
        Transaction tx = getOrThrow(transactionId);
        assertActor(tx.getSellerId(), sellerId);

        long delayHours = adminSettingsService.getLong("DEFAULT_CONFIRMATION_DELAY_HOURS", defaultConfirmationDelayHoursFallback);
        tx.setDeliveredAt(Instant.now());
        tx.setAutoReleaseAt(Instant.now().plusSeconds(delayHours * 3600));
        transitionTo(tx, TransactionStatus.EN_LIVRAISON, sellerId, "DELIVERY_DECLARED", Map.of());
        transactionRepository.save(tx);
        // Passage direct au statut d'attente de confirmation
        transitionTo(tx, TransactionStatus.A_CONFIRMER, sellerId, "AWAITING_BUYER_CONFIRMATION", Map.of());
        tx = transactionRepository.save(tx);

        // 🔥 FORCER LE FLUSH AVANT LA NOTIFICATION ASYNCHRONE
        transactionRepository.flush();

        // Règle métier #6/§8.5 : générer et envoyer l'OTP de confirmation si ce mode est choisi.
        if (tx.getConfirmationMode() == ConfirmationMode.OTP) {
            String otp = otpService.generate(confirmationOtpKey(tx.getId()));
            notify(tx.getBuyerId(), tx.getId(), NOTIF_AWAITING_CONFIRMATION,
                    "Livraison déclarée pour \"" + tx.getTitle() + "\". Votre code de confirmation : " + otp);
        } else {
            notify(tx.getBuyerId(), tx.getId(), NOTIF_AWAITING_CONFIRMATION,
                    "Livraison déclarée pour \"" + tx.getTitle() + "\". Merci de confirmer la réception.");
        }
        return tx;
    }

    /**
     * UC-05 : l'acheteur confirme la réception (bouton, OTP ou code secret — §8.5).
     * @param confirmationCode requis si le mode est OTP ou CODE_SECRET ; ignoré pour BUTTON.
     */
    @Transactional
    public Transaction confirm(UUID transactionId, UUID buyerId, String confirmationCode) {
        Transaction tx = getOrThrow(transactionId);
        assertActor(tx.getBuyerId(), buyerId);
        assertConfirmationCodeValid(tx, confirmationCode);

        tx.setConfirmedAt(Instant.now());
        tx.setCompletedAt(Instant.now());
        transitionTo(tx, TransactionStatus.TERMINEE, buyerId, "BUYER_CONFIRMED", Map.of());
        tx = transactionRepository.save(tx);

        // 🔥 FORCER LE FLUSH AVANT LA NOTIFICATION ASYNCHRONE
        transactionRepository.flush();

        notify(tx.getSellerId(), tx.getId(), NOTIF_TX_COMPLETED,
                "Réception confirmée pour \"" + tx.getTitle() + "\". Les fonds sont libérés.");
        return tx;
    }

    /** Règle métier #6/§8.5 : vérifie l'OTP ou le code secret selon le mode choisi à la création. */
    private void assertConfirmationCodeValid(Transaction tx, String providedCode) {
        switch (tx.getConfirmationMode()) {
            case BUTTON -> { /* aucune vérification nécessaire */ }
            case OTP -> {
                if (providedCode == null || !otpService.verify(confirmationOtpKey(tx.getId()), providedCode)) {
                    throw new BusinessException("OTP_INVALID", "Code OTP de confirmation invalide ou expiré");
                }
            }
            case CODE_SECRET -> {
                if (providedCode == null || tx.getConfirmationSecretHash() == null
                        || !passwordEncoder.matches(providedCode, tx.getConfirmationSecretHash())) {
                    throw new BusinessException("SECRET_INVALID", "Code secret de confirmation invalide");
                }
            }
        }
    }

    private String confirmationOtpKey(UUID transactionId) {
        return "confirm:" + transactionId;
    }

    private void transitionTo(Transaction tx, TransactionStatus newStatus, UUID actorId,
                              String eventType, Map<String, Object> payload) {
        stateMachine.assertTransitionAllowed(tx.getStatus(), newStatus);
        TransactionStatus previous = tx.getStatus();
        tx.setStatus(newStatus);
        tx.setUpdatedAt(Instant.now());
        auditLogger.logTransactionEvent(tx.getId(), eventType, previous.name(), newStatus.name(), actorId, payload);

        // Règle métier #21 : la réputation évolue selon les transactions réussies.
        // Point d'entrée unique : toute transaction qui atteint TERMINEE passe forcément par ici,
        // quel que soit le chemin (confirmation acheteur, relâche auto, décision d'arbitrage).
        if (newStatus == TransactionStatus.TERMINEE) {
            reputationService.recordSuccessfulTransaction(tx.getBuyerId(), tx.getSellerId());
        }
    }

    private void assertActor(UUID expected, UUID actual) {
        if (!expected.equals(actual)) {
            throw new BusinessException("FORBIDDEN_ACTOR", "Cet utilisateur n'est pas autorisé pour cette action");
        }
    }

    /**
     * Applique une transition décidée par un administrateur (ex : issue d'un
     * litige, relâche automatique). Contrairement aux autres méthodes, l'acteur
     * n'est pas vérifié ici : c'est à l'appelant (DisputeService, AutoReleaseJob)
     * de garantir l'autorisation.
     */
    @Transactional
    public Transaction applyAdminTransition(UUID transactionId, TransactionStatus newStatus,
                                            UUID adminId, String eventType, Map<String, Object> payload) {
        Transaction tx = getOrThrow(transactionId);
        transitionTo(tx, newStatus, adminId, eventType, payload);
        return transactionRepository.save(tx);
    }

    /**
     * Envoie une notification de manière asynchrone.
     * Les erreurs sont capturées pour ne pas interrompre le flux métier.
     */
    private void notify(UUID userId, UUID transactionId, String templateCode, String message) {
        try {
            User user = userService.getById(userId);
            notificationService.notify(userId, transactionId, NotificationChannel.SMS,
                    templateCode, user.getPhone(), message);
        } catch (Exception e) {
            // Règle métier #23 : les notifications ne doivent jamais interrompre le flux métier,
            // même en cas d'échec (utilisateur introuvable, service indisponible, etc.).
            // Log l'erreur mais continue l'exécution
        }
    }

    public Transaction getOrThrow(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction introuvable"));
    }

    public Transaction getByPublicRef(String publicRef) {
        return transactionRepository.findByPublicRef(publicRef)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction introuvable"));
    }
}