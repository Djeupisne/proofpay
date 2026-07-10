package com.proofpay.payment.application;

import com.proofpay.common.exception.BusinessException;
import com.proofpay.common.exception.ResourceNotFoundException;
import com.proofpay.notification.application.NotificationService;
import com.proofpay.notification.domain.NotificationChannel;
import com.proofpay.payment.domain.Payment;
import com.proofpay.payment.domain.PaymentStatus;
import com.proofpay.payment.infrastructure.PaymentRepository;
import com.proofpay.payment.provider.PaymentCallback;
import com.proofpay.payment.provider.PaymentInitResult;
import com.proofpay.payment.provider.PaymentProvider;
import com.proofpay.transaction.application.TransactionService;
import com.proofpay.transaction.domain.Transaction;
import com.proofpay.user.application.UserService;
import com.proofpay.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * UC-03 : payer une transaction. Le statut PAYE n'est appliqué qu'après
 * confirmation du paiement (règle métier #3, #25). Callbacks idempotents
 * (règle métier #16, §8 spécifications techniques).
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProviderRegistry providerRegistry;
    private final TransactionService transactionService;
    private final NotificationService notificationService;
    private final UserService userService;

    public PaymentService(PaymentRepository paymentRepository,
                           PaymentProviderRegistry providerRegistry,
                           TransactionService transactionService,
                           NotificationService notificationService,
                           UserService userService) {
        this.paymentRepository = paymentRepository;
        this.providerRegistry = providerRegistry;
        this.transactionService = transactionService;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @Transactional
    public Payment initiate(UUID transactionId, String payerPhone) {
        Transaction tx = transactionService.getOrThrow(transactionId);
        PaymentProvider provider = providerRegistry.getActiveProvider();

        String requestRef = "PAY-" + UUID.randomUUID();
        PaymentInitResult initResult = provider.initPayment(requestRef, tx.getAmount().add(tx.getFees()),
                tx.getCurrency(), payerPhone);

        Payment payment = Payment.builder()
                .transactionId(transactionId)
                .providerCode(provider.getProviderCode())
                .requestRef(requestRef)
                .providerRef(initResult.providerRef())
                .amount(tx.getAmount().add(tx.getFees()))
                .currency(tx.getCurrency())
                .status(PaymentStatus.INITIATED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return paymentRepository.save(payment);
    }

    /**
     * Réception d'un callback prestataire. Idempotent : si le paiement est déjà
     * CONFIRMED ou FAILED, le callback est ignoré sans effet de bord.
     */
    @Transactional
    public void handleCallback(String providerCode, Map<String, Object> rawPayload) {
        PaymentProvider provider = providerRegistry.getByCode(providerCode);
        if (provider == null) {
            throw new BusinessException("UNKNOWN_PROVIDER", "Prestataire de paiement inconnu : " + providerCode);
        }
        PaymentCallback callback = provider.parseCallback(rawPayload);

        Payment payment = paymentRepository.findByProviderRef(callback.providerRef())
                .orElseThrow(() -> new ResourceNotFoundException("Paiement introuvable pour ce callback"));

        // Idempotence : un paiement déjà dans un état terminal n'est plus modifiable
        if (payment.getStatus() == PaymentStatus.CONFIRMED || payment.getStatus() == PaymentStatus.FAILED) {
            return;
        }

        payment.setStatus(callback.status());
        payment.setCallbackPayload(callback.rawPayload());
        payment.setUpdatedAt(Instant.now());

        if (callback.status() == PaymentStatus.CONFIRMED) {
            payment.setPaidAt(Instant.now());
            paymentRepository.save(payment);
            // Règle métier #3 : le statut PAYE n'est validé qu'après confirmation du paiement.
            // Les notifications buyer/seller sont envoyées depuis TransactionService.markPaid().
            transactionService.markPaid(payment.getTransactionId());
        } else {
            // Règle métier #25 : un paiement échoué ne change pas le statut vers PAYE.
            paymentRepository.save(payment);
            notifyPaymentFailed(payment);
        }
    }

    private void notifyPaymentFailed(Payment payment) {
        try {
            Transaction tx = transactionService.getOrThrow(payment.getTransactionId());
            User buyer = userService.getById(tx.getBuyerId());
            notificationService.notify(buyer.getId(), tx.getId(), NotificationChannel.SMS, "PAYMENT_FAILED",
                    buyer.getPhone(), "Le paiement pour \"" + tx.getTitle() + "\" a échoué. Merci de réessayer.");
        } catch (Exception e) {
            // Règle métier #23 : une notification en échec ne doit jamais interrompre le flux métier.
        }
    }
}
