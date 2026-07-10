-- ================================
-- PROOFPAY - V9 : ENUM PostgreSQL natifs -> VARCHAR
-- ================================
-- Hibernate 6 lie par défaut les paramètres d'énumération Java en VARCHAR.
-- PostgreSQL n'a pas d'opérateur "=" implicite entre un type enum personnalisé
-- (transaction_status, payment_status, ...) et un varchar, ce qui provoque
-- "l'opérateur n'existe pas : transaction_status = character varying" dès
-- qu'une requête utilise un paramètre lié sur ces colonnes (cf. jobs planifiés).
-- On convertit donc ces colonnes en VARCHAR ; @Enumerated(EnumType.STRING)
-- côté JPA reste inchangé et redevient nativement compatible.

-- 1. Supprimer l'index partiel qui dépend du type enum avant conversion
DROP INDEX IF EXISTS uq_disputes_active_per_transaction;

-- 2. users.status
ALTER TABLE users ALTER COLUMN status DROP DEFAULT;
ALTER TABLE users ALTER COLUMN status TYPE VARCHAR(30) USING status::text;
ALTER TABLE users ALTER COLUMN status SET DEFAULT 'PENDING_VERIFICATION';

-- 3. transactions.status et transactions.confirmation_mode
ALTER TABLE transactions ALTER COLUMN status DROP DEFAULT;
ALTER TABLE transactions ALTER COLUMN status TYPE VARCHAR(30) USING status::text;
ALTER TABLE transactions ALTER COLUMN status SET DEFAULT 'BROUILLON';

ALTER TABLE transactions ALTER COLUMN confirmation_mode DROP DEFAULT;
ALTER TABLE transactions ALTER COLUMN confirmation_mode TYPE VARCHAR(20) USING confirmation_mode::text;
ALTER TABLE transactions ALTER COLUMN confirmation_mode SET DEFAULT 'BUTTON';

-- 4. payments.status
ALTER TABLE payments ALTER COLUMN status DROP DEFAULT;
ALTER TABLE payments ALTER COLUMN status TYPE VARCHAR(20) USING status::text;
ALTER TABLE payments ALTER COLUMN status SET DEFAULT 'INITIATED';

-- 5. disputes.status
ALTER TABLE disputes ALTER COLUMN status DROP DEFAULT;
ALTER TABLE disputes ALTER COLUMN status TYPE VARCHAR(20) USING status::text;
ALTER TABLE disputes ALTER COLUMN status SET DEFAULT 'OPEN';

-- 6. notifications.channel
ALTER TABLE notifications ALTER COLUMN channel TYPE VARCHAR(20) USING channel::text;

-- 7. Recréer l'index partiel (règle métier #29 : un seul litige actif à la fois)
CREATE UNIQUE INDEX uq_disputes_active_per_transaction
    ON disputes(transaction_id)
    WHERE status IN ('OPEN', 'UNDER_REVIEW');
