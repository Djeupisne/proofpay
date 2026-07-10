package com.proofpay.transaction.domain;

/** Machine d'état — §7 spécifications fonctionnelles. */
public enum TransactionStatus {
    BROUILLON,
    EN_ATTENTE_ACCEPTATION,
    REFUSEE,
    EN_ATTENTE_PAIEMENT,
    PAYE,
    EN_LIVRAISON,
    A_CONFIRMER,
    RELACHE_AUTO,
    LITIGE,
    TERMINEE,
    REMBOURSEE,
    ANNULEE,
    EXPIREE
}
