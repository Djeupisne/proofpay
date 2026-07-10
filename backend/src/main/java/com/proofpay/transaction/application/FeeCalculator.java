package com.proofpay.transaction.application;

import com.proofpay.admin.application.AdminSettingsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calcule les frais de service — règle métier #20 : les frais doivent être
 * connus avant paiement. Formule : max(montant * pourcentage, minimum).
 * Les taux sont lus dynamiquement depuis admin_settings (SERVICE_FEE_PERCENT,
 * SERVICE_FEE_MIN_XOF) : une modification via l'API admin est donc immédiate,
 * contrairement à une valeur figée dans application.yml. Les valeurs de
 * application.yml ne servent que de repli si la ligne est absente en base.
 */
@Component
public class FeeCalculator {

    private final AdminSettingsService adminSettingsService;
    private final BigDecimal feePercentFallback;
    private final BigDecimal feeMinXofFallback;

    public FeeCalculator(AdminSettingsService adminSettingsService,
                          @Value("${proofpay.transaction.service-fee-percent}") BigDecimal feePercentFallback,
                          @Value("${proofpay.transaction.service-fee-min-xof}") BigDecimal feeMinXofFallback) {
        this.adminSettingsService = adminSettingsService;
        this.feePercentFallback = feePercentFallback;
        this.feeMinXofFallback = feeMinXofFallback;
    }

    public BigDecimal computeFees(BigDecimal amount) {
        BigDecimal feePercent = adminSettingsService.getDecimal("SERVICE_FEE_PERCENT", feePercentFallback);
        BigDecimal feeMinXof = adminSettingsService.getDecimal("SERVICE_FEE_MIN_XOF", feeMinXofFallback);

        BigDecimal computed = amount.multiply(feePercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return computed.max(feeMinXof);
    }
}
