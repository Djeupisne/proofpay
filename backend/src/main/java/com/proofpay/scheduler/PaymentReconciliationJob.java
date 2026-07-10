package com.proofpay.scheduler;

import com.proofpay.payment.domain.Payment;
import com.proofpay.payment.domain.PaymentStatus;
import com.proofpay.payment.infrastructure.PaymentRepository;
import com.proofpay.payment.provider.PaymentProvider;
import com.proofpay.payment.application.PaymentProviderRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Rapprochement périodique des paiements restés trop longtemps en attente
 * (§10 spécifications techniques et §17 risques : double traitement / callback perdu).
 */
@Component
public class PaymentReconciliationJob {

    private static final long STALE_THRESHOLD_MINUTES = 30;

    private final PaymentRepository paymentRepository;
    private final PaymentProviderRegistry providerRegistry;

    public PaymentReconciliationJob(PaymentRepository paymentRepository, PaymentProviderRegistry providerRegistry) {
        this.paymentRepository = paymentRepository;
        this.providerRegistry = providerRegistry;
    }

    @Scheduled(fixedRate = 600_000) // toutes les 10 minutes
    public void reconcilePendingPayments() {
        Instant threshold = Instant.now().minusSeconds(STALE_THRESHOLD_MINUTES * 60);
        List<Payment> stalePayments = paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.PENDING, threshold);

        for (Payment payment : stalePayments) {
            PaymentProvider provider = providerRegistry.getByCode(payment.getProviderCode());
            if (provider != null && provider.verifyPayment(payment.getProviderRef())) {
                payment.setStatus(PaymentStatus.CONFIRMED);
                paymentRepository.save(payment);
            }
        }
    }
}
