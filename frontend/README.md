# ProofPay — Frontend (Angular)

Application mobile-first (SPA/PWA) pour le MVP ProofPay.

## Stack
- Angular 18 (standalone components, signals)
- Architecture feature-based : `core/` (transverse), `shared/` (UI réutilisable), `features/` (écrans métier)

## Démarrer en local

```bash
npm install
npm start
```

L'app démarre sur `http://localhost:4200` et cible l'API sur
`http://localhost:8080/api` (voir `src/environments/environment.ts`).

## Structure

- `core/auth` — connexion OTP, guards de routes
- `core/http` — intercepteurs JWT, gestion d'erreurs, idempotence
- `core/services` — clients HTTP par domaine (transaction, payment, dispute)
- `features/transactions` — création, liste, détail/suivi
- `features/disputes` — ouverture de litige
- `features/admin` — recherche support minimale

## À faire avant la production
- PWA : configurer `ngsw-config.json` et le manifest pour l'installation mobile.
- Remplacer le stockage du JWT en localStorage par une stratégie plus robuste (httpOnly cookie via un BFF, par ex.).
- Ajouter les écrans manquants : settings admin, décision de litige (UI dédiée), historique de notifications.
