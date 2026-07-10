# ProofPay — Backend (Spring Boot)

API REST du MVP ProofPay : escrow digital pour transactions entre particuliers
et petits vendeurs/prestataires.

## Stack
- Java 21, Spring Boot 3.3
- PostgreSQL + Flyway (migrations dans `src/main/resources/db/migration`)
- Sécurité : JWT + OTP
- Architecture : modules métier (`user`, `transaction`, `payment`, `dispute`,
  `attachment`, `notification`, `admin`) en couches `api / application / domain / infrastructure`.

## Démarrer en local

```bash
# 1. Lancer PostgreSQL (via docker-compose à la racine du repo)
docker compose -f ../docker/docker-compose.yml up -d db

# 2. Lancer l'application (profil local)
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

L'API démarre sur `http://localhost:8080`. Les migrations Flyway s'appliquent
automatiquement au démarrage.

## Tests

```bash
mvn test
```

## Points d'entrée principaux
- `POST /api/auth/request-otp`, `POST /api/auth/verify-otp`
- `POST /api/transactions`, `/accept`, `/reject`, `/pay`, `/mark-delivered`, `/confirm`
- `POST /api/payments/callback/{provider}` (idempotent)
- `POST /api/transactions/{id}/open-dispute`, `POST /api/disputes/{id}/decision`
- `GET /api/admin/transactions`, `GET/PUT /api/admin/settings`

## Ce qui reste à faire avant la production
- Remplacer le stockage OTP/idempotency-key en mémoire par Redis.
- Implémenter les vrais adaptateurs de paiement (MTN, Moov, Orange) derrière `PaymentProvider`.
- Ajouter le rate-limiting sur `/api/auth/*`.
- Chiffrer les champs sensibles au repos si nécessaire (`confirmation_secret_hash` est déjà hashé).

## Bootstrap du premier compte administrateur
Aucun utilisateur n'a le rôle ADMIN par défaut. Pour créer le premier admin,
ajoutez son numéro de téléphone à la variable d'environnement
`PROOFPAY_ADMIN_PHONES` (ou `proofpay.admin.bootstrap-phones` dans
`application.yml`), puis connectez-vous avec ce numéro via `/api/auth/verify-otp` :
le rôle ADMIN est attribué automatiquement à cette connexion. Les rôles
peuvent ensuite être gérés via `POST /api/admin/users/{id}/role` (réservé aux
administrateurs déjà en place).

```bash
export PROOFPAY_ADMIN_PHONES="+22890000000"
```

## Paramètres métier dynamiques
`SERVICE_FEE_PERCENT`, `SERVICE_FEE_MIN_XOF` et `DEFAULT_CONFIRMATION_DELAY_HOURS`
sont lus depuis la table `admin_settings` à chaque calcul (pas de cache), donc
`PUT /api/admin/settings/{key}` a un effet immédiat sans redéploiement. Les
valeurs de `application.yml` ne servent que de repli si la ligne est absente.
