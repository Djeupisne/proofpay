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
    
    // ✅ Une seule définition - retourne Optional car un utilisateur n'a qu'un abonnement actif
    Optional<Subscription> findByUser_IdAndActiveTrue(UUID userId);
    
    // ✅ Pour la liste des abonnements qui expirent
    List<Subscription> findByActiveTrueAndEndDateBefore(Instant now);
    
    // ✅ Vérification d'existence
    boolean existsByUser_IdAndActiveTrue(UUID userId);
}
