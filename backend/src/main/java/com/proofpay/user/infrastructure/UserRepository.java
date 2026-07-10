package com.proofpay.user.infrastructure;

import com.proofpay.user.domain.User;
import com.proofpay.user.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);

    // Tableau de bord admin
    long countByStatus(UserStatus status);
}
