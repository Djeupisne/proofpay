package com.proofpay.payment.provider;

/** Résultat d'une initialisation de paiement auprès d'un prestataire externe. */
public record PaymentInitResult(String providerRef, String redirectOrUssdInfo) {
}
