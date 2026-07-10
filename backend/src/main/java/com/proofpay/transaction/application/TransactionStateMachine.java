package com.proofpay.transaction.application;

import com.proofpay.common.exception.BusinessException;
import com.proofpay.transaction.domain.TransactionStatus;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;

import static com.proofpay.transaction.domain.TransactionStatus.*;

/**
 * Centralise TOUTES les transitions d'état de la transaction, conformément à
 * la règle technique §8 : "Les transitions d'état doivent être centralisées
 * dans un service métier unique pour éviter les incohérences."
 * Cf. tableau des états — §7 spécifications fonctionnelles.
 */
@Component
public class TransactionStateMachine {

    private static final Map<TransactionStatus, EnumSet<TransactionStatus>> ALLOWED_TRANSITIONS = Map.ofEntries(
            Map.entry(BROUILLON, EnumSet.of(EN_ATTENTE_ACCEPTATION, ANNULEE)),
            Map.entry(EN_ATTENTE_ACCEPTATION, EnumSet.of(EN_ATTENTE_PAIEMENT, ANNULEE, REFUSEE)),
            Map.entry(EN_ATTENTE_PAIEMENT, EnumSet.of(PAYE, ANNULEE, EXPIREE)),
            Map.entry(PAYE, EnumSet.of(EN_LIVRAISON, LITIGE)),
            Map.entry(EN_LIVRAISON, EnumSet.of(A_CONFIRMER, LITIGE)),
            Map.entry(A_CONFIRMER, EnumSet.of(TERMINEE, LITIGE, RELACHE_AUTO)),
            Map.entry(RELACHE_AUTO, EnumSet.of(TERMINEE)),
            Map.entry(LITIGE, EnumSet.of(REMBOURSEE, TERMINEE, ANNULEE)),
            // États terminaux : aucune transition sortante
            Map.entry(TERMINEE, EnumSet.noneOf(TransactionStatus.class)),
            Map.entry(REMBOURSEE, EnumSet.noneOf(TransactionStatus.class)),
            Map.entry(ANNULEE, EnumSet.noneOf(TransactionStatus.class)),
            Map.entry(REFUSEE, EnumSet.noneOf(TransactionStatus.class)),
            Map.entry(EXPIREE, EnumSet.noneOf(TransactionStatus.class))
    );

    /** Ouverture d'un litige possible depuis PAYE, EN_LIVRAISON ou A_CONFIRMER (§8.6). */
    private static final EnumSet<TransactionStatus> DISPUTE_ALLOWED_FROM =
            EnumSet.of(PAYE, EN_LIVRAISON, A_CONFIRMER);

    public void assertTransitionAllowed(TransactionStatus from, TransactionStatus to) {
        EnumSet<TransactionStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, EnumSet.noneOf(TransactionStatus.class));
        if (!allowed.contains(to)) {
            throw new BusinessException(
                    "INVALID_TRANSITION",
                    "Transition interdite : " + from + " -> " + to
            );
        }
    }

    public void assertDisputeOpenable(TransactionStatus current) {
        if (!DISPUTE_ALLOWED_FROM.contains(current)) {
            throw new BusinessException(
                    "DISPUTE_NOT_ALLOWED",
                    "Un litige ne peut pas être ouvert depuis le statut " + current
            );
        }
    }
}
