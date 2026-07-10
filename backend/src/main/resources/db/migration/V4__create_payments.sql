-- ================================
-- PROOFPAY - V4 : Paiements et tentatives
-- ================================
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    provider_code VARCHAR(50) NOT NULL,
    channel VARCHAR(30),
    request_ref VARCHAR(100) NOT NULL UNIQUE,
    provider_ref VARCHAR(100),
    amount NUMERIC(18,2) NOT NULL CHECK (amount > 0),
    currency CHAR(3) NOT NULL DEFAULT 'XOF',
    status payment_status NOT NULL DEFAULT 'INITIATED',
    failure_reason TEXT,
    callback_payload JSONB,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payment_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    attempt_no INTEGER NOT NULL,
    result_code VARCHAR(50),
    result_message TEXT,
    raw_payload JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(payment_id, attempt_no)
);

CREATE INDEX idx_payments_tx ON payments(transaction_id);
CREATE INDEX idx_payments_provider_ref ON payments(provider_ref);
