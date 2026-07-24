package com.proofpay.user.domain;

/** Contrôle d'accès par rôle : USER, SELLER, ADMIN, SUPPORT (§9 spécifications techniques). */
public enum UserRole {
    USER,
    SELLER,    // 🔥 AJOUTÉ
    ADMIN,
    SUPPORT,
    BUYER      // 🔥 AJOUTÉ
}
