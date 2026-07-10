package com.proofpay.admin.application;

import com.proofpay.transaction.domain.Transaction;
import com.proofpay.transaction.infrastructure.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Recherche support multi-critères (§8.8 : téléphone, référence, statut, date, montant). */
@Service
public class AdminReportingService {

    private final TransactionRepository transactionRepository;

    public AdminReportingService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** §13 : "les endpoints les plus utilisés doivent être paginés". */
    public Page<Transaction> searchByUser(UUID userId, Pageable pageable) {
        return transactionRepository.findByBuyerIdOrSellerIdOrderByCreatedAtDesc(userId, userId, pageable);
    }

    public Transaction searchByRef(String publicRef) {
        return transactionRepository.findByPublicRef(publicRef).orElse(null);
    }
}
