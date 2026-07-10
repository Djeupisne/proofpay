package com.proofpay.admin.application;

import com.proofpay.dispute.domain.DisputeStatus;
import com.proofpay.dispute.infrastructure.DisputeRepository;
import com.proofpay.transaction.domain.TransactionStatus;
import com.proofpay.transaction.infrastructure.TransactionRepository;
import com.proofpay.user.domain.UserStatus;
import com.proofpay.user.infrastructure.UserRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Indicateurs clés pour le tableau de bord support/admin (§8.8 spécifications
 * fonctionnelles : "supervision"). Sert avant tout à repérer en un coup d'œil
 * ce qui nécessite une action humaine (litiges ouverts, comptes suspendus) —
 * central dans une application dont le but est de prévenir la fraude.
 */
@Service
public class AdminStatsService {

    private final TransactionRepository transactionRepository;
    private final DisputeRepository disputeRepository;
    private final UserRepository userRepository;

    public AdminStatsService(TransactionRepository transactionRepository,
                              DisputeRepository disputeRepository,
                              UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.disputeRepository = disputeRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> compute() {
        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("totalTransactions", transactionRepository.count());
        stats.put("totalUsers", userRepository.count());

        // Litiges nécessitant une action immédiate de l'admin.
        long openDisputes = disputeRepository.countByStatusIn(
                List.of(DisputeStatus.OPEN, DisputeStatus.UNDER_REVIEW));
        stats.put("openDisputes", openDisputes);

        // Comptes suspendus : signal direct de fraude/abus détecté (règle métier #22).
        stats.put("suspendedUsers", userRepository.countByStatus(UserStatus.SUSPENDED));
        stats.put("blockedUsers", userRepository.countByStatus(UserStatus.BLOCKED));

        // Répartition par statut, pour visualiser le flux (combien en attente
        // de paiement, en livraison, terminées, en litige...).
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (TransactionStatus status : TransactionStatus.values()) {
            byStatus.put(status.name(), transactionRepository.countByStatus(status));
        }
        stats.put("transactionsByStatus", byStatus);

        return stats;
    }
}
