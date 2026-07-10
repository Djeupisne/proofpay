# Conformité aux spécifications — état au 08/07/2026

Relecture croisée entre les 5 documents fournis (règles métier, spéc.
fonctionnelles, spéc. techniques, plan projet, dossier SQL/diagrammes) et le
code réellement présent dans ce dépôt.

## Machine d'état (§7 spéc. fonctionnelles)

Les 13 statuts et leurs transitions autorisées sont centralisés dans
`backend/.../transaction/application/TransactionStateMachine.java` — aucune
autre classe ne modifie un statut directement. Correspond exactement au
tableau du §7.

## Les 30 règles métier

Implémentées et référencées par leur numéro directement en commentaire dans
le code (`// Règle métier #N`). Points vérifiés dans cette relecture :

| # | Règle | Où |
|---|---|---|
| 1-2 | Paiement seulement après acceptation, statut `EN_ATTENTE_PAIEMENT` | `TransactionService.pay()` + state machine |
| 9-10 | Litige gèle la progression, décision réservée admin | `DisputeService` + `SecurityConfig` (`ROLE_ADMIN`) |
| 14 | Compte suspendu bloqué en création **et** acceptation | `TransactionService.create()` / `.accept()` |
| 16 | Paiements idempotents | `IdempotencyKeyFilter`, `PaymentService` |
| 23 | Échec de notification n'interrompt jamais le flux | try/catch systématique autour de `notify()` |
| 29 | Un seul litige actif par transaction | `DisputeService.open()` (vérifié avant transition) |
| 30 | Montants positifs validés | `@Positive` (API) + `CHECK (amount > 0)` (SQL) |

## UC-01 à UC-07

Couverts de bout en bout, back (contrôleurs + services) et front (écrans
Angular correspondants). Voir `frontend/src/app/features/`.

## API REST (§7 spéc. techniques)

Tous les endpoints listés sont présents. Un endpoint supplémentaire a été
ajouté, absent des spécifications mais nécessaire pour que la fonctionnalité
pièces jointes soit utilisable : `GET /api/attachments/{id}/download`
(les specs ne prévoyaient que l'upload et la liste — sans lui, un fichier
téléversé n'était jamais récupérable).

## Schéma SQL (dossier professionnel §9)

Les 11 migrations Flyway (`backend/src/main/resources/db/migration/`)
correspondent table par table, champ par champ, au DDL fourni — ENUMs,
contraintes, tous les index recommandés au §13 spéc. techniques.

## Écarts connus / hors MVP strict

- **Rate limiting** en mémoire (fonctionnel sur une seule instance ; à
  passer sur Redis pour un déploiement multi-instance).
- **Tests automatisés** en dessous de la recommandation du §16 spéc.
  techniques (unitaires/intégration/contrat/E2E) — à faire avant un pilote
  réel avec de vrais utilisateurs.
- **Paiement réel** : uniquement `MockPaymentProvider`, choix assumé du plan
  projet (§10 : "commencer avec un mock"). L'interface `PaymentProvider` est
  prête pour brancher MTN/Moov/Orange.
- **Visibilité des notes/avis après clôture uniquement** (§9 spéc.
  fonctionnelles) : la réputation est calculée mais pas explicitement
  masquée avant clôture — à vérifier selon l'écran de profil affiché.
- **Cadre réglementaire sur la détention de fonds** (risque #1 du plan
  projet) : hors périmètre technique, nécessite une revue juridique avant
  tout pilote avec de l'argent réel.

## Corrections apportées lors de cette relecture

- `ReputationService.recordDisputeLoss()` : ajout d'un `save()` explicite
  après mise à jour du compteur de litiges perdus (le comportement était
  déjà correct grâce au dirty-checking JPA dans le contexte transactionnel
  actuel, mais le code dépendait implicitement de ce contexte).
