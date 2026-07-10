-- ================================
-- PROOFPAY - V3 : Transactions et journal d'audit
-- ================================
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    public_ref VARCHAR(30) NOT NULL UNIQUE,
    buyer_id UUID NOT NULL REFERENCES users(id),
    seller_id UUID NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category_code VARCHAR(50),
    currency CHAR(3) NOT NULL DEFAULT 'XOF',
    amount NUMERIC(18,2) NOT NULL CHECK (amount > 0),
    fees NUMERIC(18,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(18,2) GENERATED ALWAYS AS (amount + fees) STORED,
    status transaction_status NOT NULL DEFAULT 'BROUILLON',
    confirmation_mode confirmation_mode NOT NULL DEFAULT 'BUTTON',
    confirmation_secret_hash VARCHAR(255),
    delivery_deadline TIMESTAMP,
    auto_release_at TIMESTAMP,
    paid_at TIMESTAMP,
    delivered_at TIMESTAMP,
    confirmed_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_buyer_seller_different CHECK (buyer_id <> seller_id)
);

CREATE TABLE transaction_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    previous_status transaction_status,
    new_status transaction_status,
    actor_user_id UUID REFERENCES users(id),
    payload JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_buyer ON transactions(buyer_id);
CREATE INDEX idx_transactions_seller ON transactions(seller_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_transactions_auto_release_at ON transactions(auto_release_at);
CREATE INDEX idx_transaction_events_tx ON transaction_events(transaction_id, created_at);
