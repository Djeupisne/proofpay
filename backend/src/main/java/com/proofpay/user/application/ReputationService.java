package com.proofpay.user.application;

import com.proofpay.user.domain.User;
import com.proofpay.user.infrastructure.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class ReputationService {

    private static final BigDecimal SUCCESS_SCORE = BigDecimal.valueOf(5);
    private static final BigDecimal DISPUTE_LOSS_SCORE = BigDecimal.valueOf(1);

    private final UserRepository userRepository;

    public ReputationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void recordSuccessfulTransaction(UUID buyerId, UUID sellerId) {
        applyScore(buyerId, SUCCESS_SCORE, true);
        applyScore(sellerId, SUCCESS_SCORE, true);
    }

    public void recordDisputeOpened(UUID openedBy) {
        userRepository.findById(openedBy).ifPresent(user -> {
            int count = user.getDisputesOpenedCount();
            user.setDisputesOpenedCount(count + 1);
            userRepository.save(user);
        });
    }

    public void recordDisputeLoss(UUID loserId) {
        userRepository.findById(loserId).ifPresent(user -> {
            int lost = user.getDisputesLostCount();
            user.setDisputesLostCount(lost + 1);
            userRepository.save(user);
        });
        applyScore(loserId, DISPUTE_LOSS_SCORE, false);
    }

    private void applyScore(UUID userId, BigDecimal score, boolean incrementTransactionsCount) {
        userRepository.findById(userId).ifPresent(user -> {
            int previousEvents = user.getTransactionsCount() + user.getDisputesLostCount();
            BigDecimal previousRating = user.getRating();

            BigDecimal newRating = previousRating.multiply(BigDecimal.valueOf(previousEvents))
                    .add(score)
                    .divide(BigDecimal.valueOf(previousEvents + 1), 2, RoundingMode.HALF_UP);
            user.setRating(newRating);

            if (incrementTransactionsCount) {
                user.setTransactionsCount(user.getTransactionsCount() + 1);
            }
            userRepository.save(user);
        });
    }
}
