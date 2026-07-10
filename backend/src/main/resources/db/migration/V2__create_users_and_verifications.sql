-- ================================
-- PROOFPAY - V2 : Comptes utilisateurs
-- ================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    display_name VARCHAR(150),
    email VARCHAR(150),
    photo_url TEXT,
    preferred_language VARCHAR(10) DEFAULT 'fr',
    status user_status NOT NULL DEFAULT 'PENDING_VERIFICATION',
    rating NUMERIC(3,2) DEFAULT 0,
    transactions_count INTEGER NOT NULL DEFAULT 0,
    disputes_opened_count INTEGER NOT NULL DEFAULT 0,
    disputes_lost_count INTEGER NOT NULL DEFAULT 0,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_verifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    verification_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    verification_value VARCHAR(255),
    expires_at TIMESTAMP,
    verified_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_verifications_user ON user_verifications(user_id);
