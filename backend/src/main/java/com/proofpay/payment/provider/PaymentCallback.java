package com.proofpay.payment.provider;

import com.proofpay.payment.domain.PaymentStatus;

import java.util.Map;

/** Callback normalisé, quel que soit le prestataire (§10 spécifications techniques). */
public record PaymentCallback(String providerRef, PaymentStatus status, Map<String, Object> rawPayload) {
}
