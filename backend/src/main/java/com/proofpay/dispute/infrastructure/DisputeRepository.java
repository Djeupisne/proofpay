package com.proofpay.dispute.infrastructure;

import com.proofpay.dispute.domain.Dispute;
import com.proofpay.dispute.domain.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

public interface DisputeRepository extends JpaRepository<Dispute, UUID> {
    // Règle métier #29 : un seul litige actif à la fois par transaction
    Optional<Dispute> findByTransactionIdAndStatusIn(UUID transactionId, List<DisputeStatus> statuses);

    List<Dispute> findByTransactionId(UUID transactionId);

    List<Dispute> findByStatusInOrderByOpenedAtAsc(List<DisputeStatus> statuses);

    // §13 spécifications techniques : pagination des endpoints les plus utilisés
    List<Dispute> findByStatusInOrderByOpenedAtAsc(List<DisputeStatus> statuses, Pageable pageable);

    // Tableau de bord admin
    long countByStatusIn(List<DisputeStatus> statuses);
}
