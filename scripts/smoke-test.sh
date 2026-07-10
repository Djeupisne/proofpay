#!/usr/bin/env bash
# ================================================================
# ProofPay — smoke test de bout en bout (UC-01 à UC-07)
#
# Vérifie, contre une instance réellement démarrée (docker compose up),
# que le parcours complet fonctionne : création, acceptation, paiement,
# livraison, confirmation, litige, arbitrage admin.
#
# Prérequis :
#   - L'API tourne sur $API_URL (par défaut http://localhost:8080)
#   - Le numéro admin ($ADMIN_PHONE) doit figurer dans la variable
#     d'environnement PROOFPAY_ADMIN_PHONES du backend AVANT de démarrer
#     celui-ci (sinon ce compte ne sera jamais promu ADMIN) :
#       docker/.env -> PROOFPAY_ADMIN_PHONES=+22890000099
#
# Usage : bash scripts/smoke-test.sh
# ================================================================
set -euo pipefail

API_URL="${API_URL:-http://localhost:8080}"
BUYER_PHONE="+22890000001"
SELLER_PHONE="+22890000002"
ADMIN_PHONE="+22890000099"

pass=0
fail=0

ok()   { echo "  ✅ $1"; pass=$((pass+1)); }
ko()   { echo "  ❌ $1"; fail=$((fail+1)); }
step() { echo ""; echo "── $1 ──"; }

# $1=phone -> renvoie "accessToken|userId" sur stdout
login() {
  local phone="$1"
  local otp_resp code verify_resp token uid
  otp_resp=$(curl -s -X POST "$API_URL/api/auth/request-otp" \
    -H "Content-Type: application/json" -d "{\"phone\":\"$phone\"}")
  code=$(echo "$otp_resp" | grep -o '"debugCode":"[0-9]*"' | grep -o '[0-9]*')
  if [ -z "$code" ]; then
    echo "IMPOSSIBLE_OTP|IMPOSSIBLE_OTP"; return
  fi
  verify_resp=$(curl -s -X POST "$API_URL/api/auth/verify-otp" \
    -H "Content-Type: application/json" -d "{\"phone\":\"$phone\",\"code\":\"$code\"}")
  token=$(echo "$verify_resp" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
  uid=$(echo "$verify_resp" | grep -o '"userId":"[^"]*"' | cut -d'"' -f4)
  echo "$token|$uid"
}

status_of() {
  echo "$1" | grep -o '"status":"[A-Z_]*"' | head -1 | cut -d'"' -f4
}
field() {
  echo "$1" | grep -o "\"$2\":\"[^\"]*\"" | head -1 | cut -d'"' -f4
}

step "0. Santé de l'API"
health=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/actuator/health") || true
[ "$health" = "200" ] && ok "GET /actuator/health -> 200" || ko "GET /actuator/health -> $health (l'API est-elle démarrée ?)"

step "1. Authentification OTP (acheteur, vendeur, admin)"
BUYER_LOGIN=$(login "$BUYER_PHONE"); BUYER_TOKEN="${BUYER_LOGIN%%|*}"; BUYER_ID="${BUYER_LOGIN##*|}"
SELLER_LOGIN=$(login "$SELLER_PHONE"); SELLER_TOKEN="${SELLER_LOGIN%%|*}"; SELLER_ID="${SELLER_LOGIN##*|}"
ADMIN_LOGIN=$(login "$ADMIN_PHONE"); ADMIN_TOKEN="${ADMIN_LOGIN%%|*}"; ADMIN_ID="${ADMIN_LOGIN##*|}"
[ -n "$BUYER_TOKEN" ] && [ "$BUYER_TOKEN" != "IMPOSSIBLE_OTP" ] && ok "Login acheteur" || ko "Login acheteur"
[ -n "$SELLER_TOKEN" ] && [ "$SELLER_TOKEN" != "IMPOSSIBLE_OTP" ] && ok "Login vendeur" || ko "Login vendeur"
[ -n "$ADMIN_TOKEN" ] && [ "$ADMIN_TOKEN" != "IMPOSSIBLE_OTP" ] && ok "Login admin" || ko "Login admin"

step "2. UC-01 : créer une transaction (acheteur)"
CREATE_RESP=$(curl -s -X POST "$API_URL/api/transactions" \
  -H "Authorization: Bearer $BUYER_TOKEN" -H "Content-Type: application/json" \
  -d "{\"buyerId\":\"$BUYER_ID\",\"sellerPhone\":\"$SELLER_PHONE\",\"title\":\"Test smoke\",\"description\":\"Smoke test automatique\",\"categoryCode\":\"AUTRE\",\"amount\":5000,\"confirmationMode\":\"BUTTON\",\"deliveryDelayHours\":48}")
TX_ID=$(field "$CREATE_RESP" "id")
[ -n "$TX_ID" ] && [ "$(status_of "$CREATE_RESP")" = "EN_ATTENTE_ACCEPTATION" ] \
  && ok "Transaction créée ($TX_ID) en EN_ATTENTE_ACCEPTATION" \
  || ko "Échec création transaction : $CREATE_RESP"

step "3. UC-02 : acceptation par le vendeur"
ACCEPT_RESP=$(curl -s -X POST "$API_URL/api/transactions/$TX_ID/accept?sellerId=$SELLER_ID" \
  -H "Authorization: Bearer $SELLER_TOKEN")
[ "$(status_of "$ACCEPT_RESP")" = "EN_ATTENTE_PAIEMENT" ] \
  && ok "Transaction acceptée -> EN_ATTENTE_PAIEMENT" \
  || ko "Échec acceptation : $ACCEPT_RESP"

step "4. UC-03 : paiement (acheteur)"
PAY_RESP=$(curl -s -X POST "$API_URL/api/transactions/$TX_ID/pay" \
  -H "Authorization: Bearer $BUYER_TOKEN" -H "Content-Type: application/json" \
  -d "{\"payerPhone\":\"$BUYER_PHONE\"}")
[ "$(field "$PAY_RESP" "status")" = "CONFIRMED" ] \
  && ok "Paiement confirmé" \
  || ko "Échec paiement (bug connu si tu n'as pas le correctif PaymentController) : $PAY_RESP"

STATUS_RESP=$(curl -s "$API_URL/api/transactions/$TX_ID" -H "Authorization: Bearer $BUYER_TOKEN")
[ "$(status_of "$STATUS_RESP")" = "PAYE" ] \
  && ok "Statut transaction -> PAYE" \
  || ko "Statut inattendu après paiement : $(status_of "$STATUS_RESP")"

step "5. UC-04 : déclaration de livraison (vendeur)"
DELIVER_RESP=$(curl -s -X POST "$API_URL/api/transactions/$TX_ID/mark-delivered?sellerId=$SELLER_ID" \
  -H "Authorization: Bearer $SELLER_TOKEN")
[ "$(status_of "$DELIVER_RESP")" = "A_CONFIRMER" ] \
  && ok "Livraison déclarée -> A_CONFIRMER" \
  || ko "Échec déclaration livraison : $DELIVER_RESP"

step "6. UC-05 : confirmation de réception (acheteur)"
CONFIRM_RESP=$(curl -s -X POST "$API_URL/api/transactions/$TX_ID/confirm?buyerId=$BUYER_ID" \
  -H "Authorization: Bearer $BUYER_TOKEN")
[ "$(status_of "$CONFIRM_RESP")" = "TERMINEE" ] \
  && ok "Transaction confirmée -> TERMINEE (fonds libérés)" \
  || ko "Échec confirmation : $CONFIRM_RESP"

step "7. UC-06 / UC-07 : litige + arbitrage (sur une 2e transaction)"
CREATE2=$(curl -s -X POST "$API_URL/api/transactions" \
  -H "Authorization: Bearer $BUYER_TOKEN" -H "Content-Type: application/json" \
  -d "{\"buyerId\":\"$BUYER_ID\",\"sellerPhone\":\"$SELLER_PHONE\",\"title\":\"Test litige\",\"description\":\"Smoke test litige\",\"categoryCode\":\"AUTRE\",\"amount\":3000,\"confirmationMode\":\"BUTTON\",\"deliveryDelayHours\":48}")
TX2_ID=$(field "$CREATE2" "id")
curl -s -X POST "$API_URL/api/transactions/$TX2_ID/accept?sellerId=$SELLER_ID" -H "Authorization: Bearer $SELLER_TOKEN" > /dev/null
curl -s -X POST "$API_URL/api/transactions/$TX2_ID/pay" -H "Authorization: Bearer $BUYER_TOKEN" \
  -H "Content-Type: application/json" -d "{\"payerPhone\":\"$BUYER_PHONE\"}" > /dev/null

DISPUTE_RESP=$(curl -s -X POST "$API_URL/api/transactions/$TX2_ID/open-dispute" \
  -H "Authorization: Bearer $BUYER_TOKEN" -H "Content-Type: application/json" \
  -d "{\"openedBy\":\"$BUYER_ID\",\"reasonCode\":\"NON_RECU\",\"reasonDetails\":\"Test smoke : produit jamais reçu\"}")
DISPUTE_ID=$(field "$DISPUTE_RESP" "id")
[ -n "$DISPUTE_ID" ] && [ "$(field "$DISPUTE_RESP" "status")" = "OPEN" ] \
  && ok "Litige ouvert ($DISPUTE_ID)" \
  || ko "Échec ouverture litige : $DISPUTE_RESP"

TX2_STATUS=$(curl -s "$API_URL/api/transactions/$TX2_ID" -H "Authorization: Bearer $BUYER_TOKEN")
[ "$(status_of "$TX2_STATUS")" = "LITIGE" ] \
  && ok "Transaction gelée -> LITIGE (règle métier #9)" \
  || ko "La transaction n'est pas passée en LITIGE : $(status_of "$TX2_STATUS")"

DECISION_RESP=$(curl -s -X POST "$API_URL/api/disputes/$DISPUTE_ID/decision" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d "{\"adminId\":\"$ADMIN_ID\",\"decisionCode\":\"REFUND_BUYER\",\"comment\":\"Smoke test\"}")
[ "$(field "$DECISION_RESP" "status")" = "RESOLVED" ] \
  && ok "Litige arbitré (remboursement acheteur)" \
  || ko "Échec arbitrage (vérifier que $ADMIN_PHONE a bien le rôle ADMIN) : $DECISION_RESP"

TX2_FINAL=$(curl -s "$API_URL/api/transactions/$TX2_ID" -H "Authorization: Bearer $BUYER_TOKEN")
[ "$(status_of "$TX2_FINAL")" = "REMBOURSEE" ] \
  && ok "Transaction -> REMBOURSEE" \
  || ko "Statut final inattendu : $(status_of "$TX2_FINAL")"

echo ""
echo "════════════════════════════════════"
echo "Résultat : $pass réussi(s), $fail échoué(s)"
echo "════════════════════════════════════"
[ "$fail" -eq 0 ] && exit 0 || exit 1
