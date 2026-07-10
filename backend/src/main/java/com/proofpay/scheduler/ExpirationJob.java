package com.proofpay.scheduler;

import com.proofpay.transaction.application.TransactionService;
import com.proofpay.transaction.domain.Transaction;
import com.proofpay.transaction.domain.TransactionStatus;
import com.proofpay.transaction.infrastructure.TransactionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Règle métier #26 : une transaction expirée ne peut plus être payée.
 * Ce job faisait défaut : le statut EXPIREE était défini dans la state
 * machine mais jamais déclenché. On considère qu'une transaction expire si
 * elle reste en EN_ATTENTE_PAIEMENT au-delà de son delivery_deadline (le même
 * délai configurable utilisé pour la livraison, faute de deadline de
 * paiement dédiée dans le schéma actuel).
 */
@Component
public class ExpirationJob {

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    public ExpirationJob(TransactionRepository transactionRepository, TransactionService transactionService) {
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
    }

    @Scheduled(fixedRate = 300_000) // toutes les 5 minutes
    public void expireUnpaidTransactions() {
        List<Transaction> candidates = transactionRepository
                .findByStatusAndDeliveryDeadlineBefore(TransactionStatus.EN_ATTENTE_PAIEMENT, Instant.now());

        for (Transaction tx : candidates) {
            transactionService.applyAdminTransition(tx.getId(), TransactionStatus.EXPIREE,
                    null, "TX_EXPIRED", Map.of());
        }
    }
}
