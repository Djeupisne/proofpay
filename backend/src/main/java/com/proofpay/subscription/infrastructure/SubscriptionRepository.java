package com.proofpay.subscription.infrastructure;

import com.proofpay.subscription.domain.Subscription;
import com.proofpay.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByUser(User user);
    Optional<Subscription> findByUser_Id(UUID userId);
    Optional<Subscription> findByUser_IdAndActiveTrue(UUID userId);  // ✅ Retourne Optional
    List<Subscription> findByActiveTrueAndEndDateBefore(Instant now);
    List<Subscription> findByUser_IdAndActiveTrue(UUID userId);
    boolean existsByUser_IdAndActiveTrue(UUID userId);
}
