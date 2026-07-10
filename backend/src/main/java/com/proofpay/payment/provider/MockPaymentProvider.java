package com.proofpay.payment.provider;

import com.proofpay.payment.domain.PaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Prestataire simulé pour le développement et les démonstrations
 * (§10 : "Commencer avec un MockPaymentProvider pour le développement").
 * Toute demande est automatiquement confirmée.
 */
@Component
public class MockPaymentProvider implements PaymentProvider {

    @Override
    public String getProviderCode() {
        return "mock";
    }

    @Override
    public PaymentInitResult initPayment(String requestRef, BigDecimal amount, String currency, String payerPhone) {
        String providerRef = "MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentInitResult(providerRef, "Paiement simulé — confirmation automatique");
    }

    @Override
    public PaymentCallback parseCallback(Map<String, Object> rawPayload) {
        String providerRef = (String) rawPayload.get("providerRef");
        String statusValue = (String) rawPayload.getOrDefault("status", "CONFIRMED");
        return new PaymentCallback(providerRef, PaymentStatus.valueOf(statusValue), rawPayload);
    }

    @Override
    public boolean verifyPayment(String providerRef) {
        return true; // Toujours confirmé en mode mock
    }

    @Override
    public void refund(String providerRef, BigDecimal amount) {
        // No-op en mode mock
    }
}
