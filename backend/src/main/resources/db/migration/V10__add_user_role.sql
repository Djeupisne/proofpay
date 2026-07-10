-- ================================
-- PROOFPAY - V10 : Rôle utilisateur (contrôle d'accès USER/ADMIN/SUPPORT)
-- ================================
-- Manquait entièrement : aucun utilisateur ne pouvait obtenir le rôle ADMIN,
-- rendant tout le module /api/admin/** inatteignable (hasRole("ADMIN") ne
-- passait jamais). Le rôle est promu automatiquement à la connexion pour les
-- numéros listés dans proofpay.admin.bootstrap-phones (cf. application.yml),
-- et peut ensuite être géré via POST /api/admin/users/{id}/role.
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
