package com.proofpay.user.infrastructure;

import com.proofpay.user.domain.SellerProfile;
import com.proofpay.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SellerProfileRepository extends JpaRepository<SellerProfile, UUID> {
    Optional<SellerProfile> findByUser(User user);
    Optional<SellerProfile> findByUser_Id(UUID userId);
    boolean existsByUser_Id(UUID userId);
}
