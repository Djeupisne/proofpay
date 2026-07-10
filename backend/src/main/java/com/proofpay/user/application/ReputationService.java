package com.proofpay.user.application;

import com.proofpay.user.domain.User;
import com.proofpay.user.infrastructure.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Règle métier #21 : la réputation utilisateur évolue selon les transactions
 * réussies. Historique de réputation — note moyenne, litiges gagnés/perdus
 * (§8.1 spécifications fonctionnelles).
 *
 * Heuristique MVP : chaque transaction menée à TERMINEE compte comme une
 * note de 5/5 ; chaque litige perdu compte comme une note de 1/5. La moyenne
 * est recalculée à chaque évènement ("moyenne mobile" pondérée par le nombre
 * total d'évènements notés). Cette formule est volontairement simple et
 * documentée ici pour être affinée plus tard (ex : notation explicite par
 * l'autre partie) sans changer l'API des méthodes ci-dessous.
 */
@Service
public class ReputationService {

    private static final BigDecimal SUCCESS_SCORE = BigDecimal.valueOf(5);
    private static final BigDecimal DISPUTE_LOSS_SCORE = BigDecimal.valueOf(1);

    private final UserRepository userRepository;

    public ReputationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Appelé une seule fois, au moment où une transaction atteint TERMINEE. */
    public void recordSuccessfulTransaction(UUID buyerId, UUID sellerId) {
        applyScore(buyerId, SUCCESS_SCORE, true);
        applyScore(sellerId, SUCCESS_SCORE, true);
    }

    /** Règle métier #22 : trace le nombre de litiges ouverts par un utilisateur. */
    public void recordDisputeOpened(UUID openedBy) {
        userRepository.findById(openedBy).ifPresent(user -> {
            int count = user.getDisputesOpenedCount() == null ? 0 : user.getDisputesOpenedCount();
            user.setDisputesOpenedCount(count + 1);
            userRepository.save(user);
        });
    }

    /** Appelé quand un litige est arbitré en défaveur d'un utilisateur (§8.6). */
    public void recordDisputeLoss(UUID loserId) {
        userRepository.findById(loserId).ifPresent(user -> {
            int lost = user.getDisputesLostCount() == null ? 0 : user.getDisputesLostCount();
            user.setDisputesLostCount(lost + 1);
            userRepository.save(user);
        });
        applyScore(loserId, DISPUTE_LOSS_SCORE, false);
    }

    private void applyScore(UUID userId, BigDecimal score, boolean incrementTransactionsCount) {
        userRepository.findById(userId).ifPresent(user -> {
            int previousEvents = (user.getTransactionsCount() == null ? 0 : user.getTransactionsCount())
                    + (user.getDisputesLostCount() == null ? 0 : user.getDisputesLostCount());
            BigDecimal previousRating = user.getRating() == null ? BigDecimal.ZERO : user.getRating();

            BigDecimal newRating = previousRating.multiply(BigDecimal.valueOf(previousEvents))
                    .add(score)
                    .divide(BigDecimal.valueOf(previousEvents + 1), 2, RoundingMode.HALF_UP);
            user.setRating(newRating);

            if (incrementTransactionsCount) {
                int count = user.getTransactionsCount() == null ? 0 : user.getTransactionsCount();
                user.setTransactionsCount(count + 1);
            }
            userRepository.save(user);
        });
    }
}
