package com.proofpay.payment.application;

import com.proofpay.payment.provider.PaymentProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Sélectionne le prestataire actif selon la configuration (proofpay.payment.active-provider). */
@Component
public class PaymentProviderRegistry {

    private final Map<String, PaymentProvider> providersByCode;
    private final String activeProviderCode;

    public PaymentProviderRegistry(List<PaymentProvider> providers,
                                    @Value("${proofpay.payment.active-provider}") String activeProviderCode) {
        this.providersByCode = providers.stream()
                .collect(Collectors.toMap(PaymentProvider::getProviderCode, Function.identity()));
        this.activeProviderCode = activeProviderCode;
    }

    public PaymentProvider getActiveProvider() {
        return providersByCode.get(activeProviderCode);
    }

    public PaymentProvider getByCode(String code) {
        return providersByCode.get(code);
    }
}
