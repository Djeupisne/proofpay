package com.proofpay.payment.provider;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Interface commune à tous les prestataires de paiement (MTN, Moov, Orange...)
 * — §10 spécifications techniques : "Définir une interface PaymentProvider
 * avec méthodes initPayment(), verifyPayment(), refund(), parseCallback()."
 */
public interface PaymentProvider {

    String getProviderCode();

    PaymentInitResult initPayment(String requestRef, BigDecimal amount, String currency, String payerPhone);

    PaymentCallback parseCallback(Map<String, Object> rawPayload);

    boolean verifyPayment(String providerRef);

    void refund(String providerRef, BigDecimal amount);
}
