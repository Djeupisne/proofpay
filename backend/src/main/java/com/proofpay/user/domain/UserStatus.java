package com.proofpay.user.domain;

/** users.status — cf. §6 spécifications techniques. */
public enum UserStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    SUSPENDED,
    BLOCKED
}
