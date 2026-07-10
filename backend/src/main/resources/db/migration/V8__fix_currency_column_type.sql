-- ================================
-- PROOFPAY - V8 : Correction du type de colonne "currency"
-- ================================
-- CHAR(3) est mappé par PostgreSQL en "bpchar", incompatible avec le type
-- VARCHAR attendu par Hibernate pour un champ String sans padding. On passe
-- en VARCHAR(3) pour que la validation de schéma JPA (ddl-auto: validate)
-- passe correctement.
ALTER TABLE transactions ALTER COLUMN currency TYPE VARCHAR(3);
ALTER TABLE payments ALTER COLUMN currency TYPE VARCHAR(3);
