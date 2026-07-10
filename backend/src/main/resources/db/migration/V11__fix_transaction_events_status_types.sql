-- ================================
-- PROOFPAY - V11 : Correction du type des colonnes previous_status et new_status
-- dans transaction_events
-- ================================
-- La migration V9 a converti toutes les colonnes ENUM en VARCHAR mais a oublié
-- transaction_events.previous_status et transaction_events.new_status.
-- Ces colonnes sont encore de type transaction_status (ENUM), ce qui provoque
-- des erreurs "column is of type transaction_status but expression is of type
-- character varying" lors des inserts.

-- 1. Convertir previous_status
ALTER TABLE transaction_events
ALTER COLUMN previous_status TYPE VARCHAR(50) USING previous_status::text;

-- 2. Convertir new_status
ALTER TABLE transaction_events
ALTER COLUMN new_status TYPE VARCHAR(50) USING new_status::text;

-- 3. Ajouter des commentaires pour documenter le changement
COMMENT ON COLUMN transaction_events.previous_status IS 'Statut précédent (VARCHAR)';
COMMENT ON COLUMN transaction_events.new_status IS 'Nouveau statut (VARCHAR)';

-- 4. (Optionnel) Nettoyer les types ENUM PostgreSQL inutilisés
-- Attention : Ne supprimez pas transaction_status si elle est encore utilisée
-- dans d'autres tables ou fonctions
-- Si transaction_status n'est plus utilisée par aucune table, vous pouvez la supprimer :
-- DROP TYPE IF EXISTS transaction_status CASCADE;