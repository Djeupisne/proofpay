# Comment tester ProofPay et vérifier que tout fonctionne

Il y a 3 niveaux de vérification, du plus rapide au plus complet.

## 1. Tests unitaires existants (30 secondes)

```bash
cd backend
mvn test
```

Couvre uniquement de la logique pure (2 classes) : `TransactionStateMachine`
(toutes les transitions autorisées/interdites) et `MockPaymentProvider`. Pas
besoin de base de données pour ça. **Ça ne prouve pas que l'application
fonctionne dans son ensemble** — juste que ces deux briques isolées sont
correctes.

## 2. Smoke test de bout en bout (2 minutes) — le plus important

C'est le test qui répond vraiment à "est-ce que tout fonctionne". Il joue le
scénario complet UC-01 à UC-07 contre l'API réellement démarrée : création,
acceptation, paiement, livraison, confirmation, litige, arbitrage admin.

```bash
# 1. Démarrer l'application
cd docker
cp .env.example .env
# éditer .env : POSTGRES_PASSWORD, JWT_SECRET (openssl rand -base64 48)
# PROOFPAY_ADMIN_PHONES=+22890000099 est déjà dans .env.example
docker compose up --build -d

# 2. Attendre que le backend soit prêt
curl http://localhost:8080/actuator/health

# 3. Lancer le smoke test
cd ..
bash scripts/smoke-test.sh
```

Le script affiche ✅/❌ à chaque étape et un résumé à la fin. **En écrivant
ce script, j'ai découvert et corrigé un bug bloquant** dans
`PaymentController` : le paiement comparait un statut enum à une chaîne de
caractères (`"EN_ATTENTE_PAIEMENT".equals(tx.getStatus())`), une comparaison
qui est *toujours* fausse en Java — donc le paiement échouait à 100% du
temps, quel que soit le statut réel de la transaction. C'est exactement le
genre de bug qu'un test unitaire isolé ne peut pas voir, mais qu'un test de
bout en bout attrape immédiatement. C'est corrigé dans le zip livré ici.

## 3. Vérification manuelle via l'interface (5 minutes)

Une fois `docker compose up` lancé :

1. Ouvrir http://localhost:4200
2. Se connecter avec un numéro (ex. `+22890000001`) — le code OTP s'affiche
   directement à l'écran en mode démo (`debugCode`), pas besoin de vrai SMS.
2. Créer une transaction, se reconnecter avec le numéro du "vendeur" dans un
   autre navigateur/onglet privé pour l'accepter.
3. Suivre le parcours : payer → déclarer la livraison → confirmer →
   vérifier que le statut passe bien à "Terminée".
4. Ouvrir un litige sur une autre transaction, se connecter avec le numéro
   admin (celui mis dans `PROOFPAY_ADMIN_PHONES`) pour l'arbitrer via
   `/admin/disputes`.

## Ce que ces 3 niveaux ne couvrent pas encore

- Pas de test automatisé sur les callbacks de paiement (idempotence),
  la relâche automatique après délai, ni l'expiration de transaction.
- Pas de test frontend (Angular) du tout.
- Le smoke test ne teste qu'un seul chemin nominal + un litige ; il ne
  couvre pas les cas d'erreur (montant négatif, compte suspendu, transaction
  déjà payée qu'on essaie de payer deux fois, etc.).

Si tu veux, je peux enchaîner sur une vraie suite de tests d'intégration
Spring Boot (Testcontainers + PostgreSQL réel) qui couvre ces cas
automatiquement à chaque modification du code — c'est la suite logique
naturelle après ce smoke test manuel.
