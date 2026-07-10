# ProofPay — Monorepo MVP

Plateforme d'escrow digital pour transactions entre particuliers et petits
vendeurs/prestataires (WhatsApp, réseaux sociaux, freelance).

## Structure du dépôt

```
proofpay/
├── backend/     Spring Boot (Java 21) — API REST, PostgreSQL, Flyway
├── frontend/    Angular 18 (standalone) — SPA mobile-first
├── docs/        Spécifications fonctionnelles/techniques, plan projet, dossier SQL
└── docker/      docker-compose.yml pour lancer l'environnement complet
```

## Démarrage rapide (tout via Docker)

```bash
cd docker
docker compose up --build
```

- Backend : http://localhost:8080
- Frontend : http://localhost:4200
- PostgreSQL : localhost:5432 (user/pass : proofpay/proofpay)

## Démarrage en développement (sans Docker pour le code applicatif)

```bash
# Base de données seule
cd docker && docker compose up -d db

# Backend
cd ../backend && mvn spring-boot:run -Dspring-boot.run.profiles=local

# Frontend (autre terminal)
cd ../frontend && npm install && npm start
```

## Modules métier (backend)

| Module | Rôle |
|---|---|
| `user` | Comptes, OTP, statut de vérification, suspension |
| `transaction` | Création, acceptation, state machine centralisée, frais |
| `payment` | Initialisation, callbacks idempotents, MockPaymentProvider |
| `dispute` | Ouverture de litige, arbitrage admin |
| `attachment` | Pièces justificatives (stockage local ou S3) |
| `notification` | Envoi async SMS/e-mail/WhatsApp |
| `admin` | Recherche support, paramètres métier |
| `scheduler` | Relâche automatique, rapprochement paiement |

## Règles métier de référence

Toutes les règles métier (`docs/ProofPay_Regles_Metiers_Completes.pdf`) sont
implémentées et traçables dans le code :
- Centralisation des transitions : `transaction/application/TransactionStateMachine.java`
- Idempotence paiement : `common/idempotency/IdempotencyKeyFilter.java` + `PaymentService.handleCallback`
- Un seul litige actif : contrainte SQL `uq_disputes_active_per_transaction` (V5) + vérification applicative
- Journal d'audit : `transaction_events` + `AuditLogger`

## Prochaines étapes (post-MVP)

Voir `docs/ProofPay_Plan_Projet_Detaille.pdf` §12 pour la roadmap : réputation
avancée, paiement partiel, arbitrage semi-automatique, intégration opérateurs
mobile money réels, application mobile native, ouverture d'API partenaires.
