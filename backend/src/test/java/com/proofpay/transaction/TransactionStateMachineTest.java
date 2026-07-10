package com.proofpay.transaction;

import com.proofpay.common.exception.BusinessException;
import com.proofpay.transaction.application.TransactionStateMachine;
import com.proofpay.transaction.domain.TransactionStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionStateMachineTest {

    private final TransactionStateMachine stateMachine = new TransactionStateMachine();

    @Test
    void allows_valid_transition_from_paye_to_en_livraison() {
        assertThatCode(() -> stateMachine.assertTransitionAllowed(
                TransactionStatus.PAYE, TransactionStatus.EN_LIVRAISON))
                .doesNotThrowAnyException();
    }

    @Test
    void rejects_transition_from_brouillon_to_paye() {
        assertThatThrownBy(() -> stateMachine.assertTransitionAllowed(
                TransactionStatus.BROUILLON, TransactionStatus.PAYE))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejects_any_transition_from_terminal_state() {
        assertThatThrownBy(() -> stateMachine.assertTransitionAllowed(
                TransactionStatus.TERMINEE, TransactionStatus.LITIGE))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void dispute_can_be_opened_from_paye_en_livraison_or_a_confirmer() {
        assertThatCode(() -> stateMachine.assertDisputeOpenable(TransactionStatus.PAYE)).doesNotThrowAnyException();
        assertThatCode(() -> stateMachine.assertDisputeOpenable(TransactionStatus.EN_LIVRAISON)).doesNotThrowAnyException();
        assertThatCode(() -> stateMachine.assertDisputeOpenable(TransactionStatus.A_CONFIRMER)).doesNotThrowAnyException();
    }

    @Test
    void dispute_cannot_be_opened_from_brouillon() {
        assertThatThrownBy(() -> stateMachine.assertDisputeOpenable(TransactionStatus.BROUILLON))
                .isInstanceOf(BusinessException.class);
    }
}
