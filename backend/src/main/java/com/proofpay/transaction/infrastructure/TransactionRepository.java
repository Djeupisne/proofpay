package com.proofpay.transaction.infrastructure;

import com.proofpay.transaction.domain.Transaction;
import com.proofpay.transaction.domain.TransactionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByPublicRef(String publicRef);

    List<Transaction> findByBuyerIdOrSellerId(UUID buyerId, UUID sellerId);

    // §13 spécifications techniques : "les endpoints les plus utilisés doivent être paginés"
    // Retourne une Page (et non une simple List) pour exposer le nombre total
    // d'éléments, indispensable pour que le frontend affiche une pagination réelle.
    org.springframework.data.domain.Page<Transaction> findByBuyerIdOrSellerIdOrderByCreatedAtDesc(
            UUID buyerId, UUID sellerId, Pageable pageable);

    // Utilisé par le job de relâche automatique (règle métier #7)
    List<Transaction> findByStatusAndAutoReleaseAtBefore(TransactionStatus status, Instant now);

    // Utilisé par le job d'expiration (règle métier #26)
    List<Transaction> findByStatusAndDeliveryDeadlineBefore(TransactionStatus status, Instant now);

    // Tableau de bord admin (§8.8 : supervision)
    long countByStatus(TransactionStatus status);
}
