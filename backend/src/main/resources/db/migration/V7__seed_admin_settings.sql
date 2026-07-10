-- ================================
-- PROOFPAY - V7 : Paramètres métier par défaut
-- ================================
INSERT INTO admin_settings(setting_key, setting_value, description) VALUES
('SERVICE_FEE_PERCENT', '2.00', 'Commission standard en pourcentage'),
('SERVICE_FEE_MIN_XOF', '100', 'Commission minimale en XOF'),
('DEFAULT_CONFIRMATION_DELAY_HOURS', '48', 'Délai avant relâche automatique'),
('MAX_ATTACHMENT_MB', '10', 'Taille max d''une pièce jointe');
