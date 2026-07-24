package com.proofpay.user.infrastructure;

import com.proofpay.user.domain.User;
import com.proofpay.user.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    
    // Recherche par téléphone
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
    
    // 🔥 NOUVEAU : Recherche par email
    Optional<User> findByEmail(String email);
    
    // 🔥 NOUVEAU : Recherche des vendeurs
    List<User> findByIsSellerTrue();
    List<User> findByIsSellerTrueAndStatus(UserStatus status);
    
    // 🔥 NOUVEAU : Recherche des acheteurs
    List<User> findByIsBuyerTrue();
    
    // Tableau de bord admin
    long countByStatus(UserStatus status);
    long countByIsSellerTrue();
    long countByIsSellerTrueAndStatus(UserStatus status);
}
