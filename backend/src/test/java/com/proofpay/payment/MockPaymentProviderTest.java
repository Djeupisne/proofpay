package com.proofpay.payment;

import com.proofpay.payment.domain.PaymentStatus;
import com.proofpay.payment.provider.MockPaymentProvider;
import com.proofpay.payment.provider.PaymentCallback;
import com.proofpay.payment.provider.PaymentInitResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MockPaymentProviderTest {

    private final MockPaymentProvider provider = new MockPaymentProvider();

    @Test
    void init_payment_generates_a_provider_reference() {
        PaymentInitResult result = provider.initPayment("REQ-1", BigDecimal.TEN, "XOF", "+22890000000");
        assertThat(result.providerRef()).startsWith("MOCK-");
    }

    @Test
    void parse_callback_defaults_to_confirmed_status() {
        PaymentCallback callback = provider.parseCallback(Map.of("providerRef", "MOCK-ABC12345"));
        assertThat(callback.status()).isEqualTo(PaymentStatus.CONFIRMED);
        assertThat(callback.providerRef()).isEqualTo("MOCK-ABC12345");
    }
}
