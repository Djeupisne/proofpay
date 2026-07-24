package com.proofpay.subscription.application;

import com.proofpay.common.exception.BusinessException;
import com.proofpay.common.exception.ResourceNotFoundException;
import com.proofpay.subscription.domain.Subscription;
import com.proofpay.subscription.domain.SubscriptionPlan;
import com.proofpay.subscription.infrastructure.SubscriptionRepository;
import com.proofpay.user.domain.User;
import com.proofpay.user.infrastructure.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    public Subscription createSubscription(UUID userId, SubscriptionPlan plan, int durationDays) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        if (!user.isSeller()) {
            throw new BusinessException("NOT_SELLER", "Seuls les vendeurs peuvent avoir un abonnement");
        }

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)  // ✅ Utiliser SubscriptionPlan directement
                .startDate(Instant.now())
                .endDate(Instant.now().plusSeconds(durationDays * 24 * 60 * 60L))
                .autoRenew(false)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return subscriptionRepository.save(subscription);
    }

    public boolean hasActiveSubscription(UUID userId) {
        return subscriptionRepository.findByUser_IdAndActiveTrue(userId)
                .isPresent();  // ✅ Retourne Optional, donc isPresent() fonctionne
    }

    public Subscription renewSubscription(UUID userId, int additionalDays) {
        Subscription subscription = subscriptionRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement introuvable"));

        if (!subscription.isActive()) {
            subscription.setActive(true);
        }

        Instant newEndDate = subscription.getEndDate().isAfter(Instant.now()) 
                ? subscription.getEndDate().plusSeconds(additionalDays * 24 * 60 * 60L)
                : Instant.now().plusSeconds(additionalDays * 24 * 60 * 60L);
        
        subscription.setEndDate(newEndDate);
        subscription.setUpdatedAt(Instant.now());
        return subscriptionRepository.save(subscription);
    }

    public void cancelSubscription(UUID userId) {
        Subscription subscription = subscriptionRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement introuvable"));
        subscription.setActive(false);
        subscription.setUpdatedAt(Instant.now());
        subscriptionRepository.save(subscription);
    }

    public Subscription getSubscription(UUID userId) {
        return subscriptionRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement introuvable"));
    }
}
