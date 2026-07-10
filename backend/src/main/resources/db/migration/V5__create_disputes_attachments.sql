-- ================================
-- PROOFPAY - V5 : Litiges et pièces justificatives
-- ================================
CREATE TABLE disputes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    opened_by UUID NOT NULL REFERENCES users(id),
    reason_code VARCHAR(50) NOT NULL,
    reason_details TEXT,
    status dispute_status NOT NULL DEFAULT 'OPEN',
    decision_code VARCHAR(50),
    decision_comment TEXT,
    resolved_by UUID REFERENCES users(id),
    opened_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

-- Un seul litige actif à la fois par transaction (règle métier #29)
CREATE UNIQUE INDEX uq_disputes_active_per_transaction
    ON disputes(transaction_id)
    WHERE status IN ('OPEN','UNDER_REVIEW');

CREATE TABLE attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_type VARCHAR(30) NOT NULL,
    owner_id UUID NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    storage_path TEXT NOT NULL,
    uploaded_by UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_disputes_tx ON disputes(transaction_id);
CREATE INDEX idx_attachments_owner ON attachments(owner_type, owner_id);
