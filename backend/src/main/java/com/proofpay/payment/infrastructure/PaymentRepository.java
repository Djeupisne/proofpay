package com.proofpay.payment.infrastructure;

import com.proofpay.payment.domain.Payment;
import com.proofpay.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByRequestRef(String requestRef);
    Optional<Payment> findByProviderRef(String providerRef);
    List<Payment> findByTransactionId(UUID transactionId);

    // Rapprochement périodique (§10 spécifications techniques)
    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, Instant threshold);
}
