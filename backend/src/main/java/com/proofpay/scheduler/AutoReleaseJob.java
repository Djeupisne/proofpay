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
 * Règle métier #7 : en absence de confirmation, libération automatique après
 * le délai configuré (transactions.auto_release_at).
 */
@Component
public class AutoReleaseJob {

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    public AutoReleaseJob(TransactionRepository transactionRepository, TransactionService transactionService) {
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
    }

    @Scheduled(fixedRate = 300_000) // toutes les 5 minutes
    public void releaseExpiredConfirmations() {
        List<Transaction> candidates = transactionRepository
                .findByStatusAndAutoReleaseAtBefore(TransactionStatus.A_CONFIRMER, Instant.now());

        for (Transaction tx : candidates) {
            transactionService.applyAdminTransition(tx.getId(), TransactionStatus.RELACHE_AUTO,
                    null, "AUTO_RELEASE_TRIGGERED", Map.of());
            transactionService.applyAdminTransition(tx.getId(), TransactionStatus.TERMINEE,
                    null, "AUTO_RELEASE_COMPLETED", Map.of());
        }
    }
}
