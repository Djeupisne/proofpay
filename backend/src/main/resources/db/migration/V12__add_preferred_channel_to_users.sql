-- Ajouter la colonne preferred_channel à la table users
ALTER TABLE users ADD COLUMN preferred_channel VARCHAR(20) DEFAULT 'SMS' NOT NULL;

-- Ajouter une contrainte pour limiter les valeurs autorisées
ALTER TABLE users ADD CONSTRAINT chk_preferred_channel 
    CHECK (preferred_channel IN ('SMS', 'EMAIL', 'WHATSAPP', 'PUSH'));

-- Mettre à jour les utilisateurs existants
UPDATE users SET preferred_channel = 'SMS' WHERE preferred_channel IS NULL;
