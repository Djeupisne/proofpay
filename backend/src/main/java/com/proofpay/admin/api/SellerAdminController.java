package com.proofpay.admin.api;

import com.proofpay.user.application.UserService;
import com.proofpay.user.domain.SellerProfile;
import com.proofpay.user.domain.User;
import com.proofpay.user.domain.UserStatus;
import com.proofpay.user.infrastructure.SellerProfileRepository;
import com.proofpay.user.infrastructure.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/sellers")
public class SellerAdminController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;

    public SellerAdminController(UserService userService,
                                 UserRepository userRepository,
                                 SellerProfileRepository sellerProfileRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.sellerProfileRepository = sellerProfileRepository;
    }

    /**
     * Liste de tous les vendeurs (admin uniquement)
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllSellers() {
        return ResponseEntity.ok(userRepository.findByIsSellerTrue());
    }

    /**
     * Approuver un vendeur (admin uniquement)
     */
    @PutMapping("/{sellerId}/approve")
    public ResponseEntity<?> approveSeller(@PathVariable UUID sellerId) {
        User user = userService.getById(sellerId);
        if (!user.isSeller()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cet utilisateur n'est pas un vendeur"));
        }

        user.setVerifiedSeller(true);
        user.setApprovedSeller(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setSellerVerifiedAt(Instant.now());
        userRepository.save(user);

        // Mettre à jour le profil
        SellerProfile profile = sellerProfileRepository.findByUser_Id(sellerId).orElse(null);
        if (profile != null) {
            profile.setVerified(true);
            profile.setApproved(true);
            profile.setApprovedAt(Instant.now());
            profile.setVerificationStatus(SellerProfile.VerificationStatus.VERIFIED);
            sellerProfileRepository.save(profile);
        }

        return ResponseEntity.ok(Map.of("message", "Vendeur approuvé avec succès"));
    }

    /**
     * Rejeter un vendeur (admin uniquement)
     */
    @PutMapping("/{sellerId}/reject")
    public ResponseEntity<?> rejectSeller(@PathVariable UUID sellerId) {
        User user = userService.getById(sellerId);
        if (!user.isSeller()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cet utilisateur n'est pas un vendeur"));
        }

        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);

        SellerProfile profile = sellerProfileRepository.findByUser_Id(sellerId).orElse(null);
        if (profile != null) {
            profile.setVerificationStatus(SellerProfile.VerificationStatus.REJECTED);
            sellerProfileRepository.save(profile);
        }

        return ResponseEntity.ok(Map.of("message", "Vendeur rejeté"));
    }

    /**
     * Obtenir les détails complets d'un vendeur (admin uniquement)
     */
    @GetMapping("/{sellerId}/details")
    public ResponseEntity<?> getSellerDetails(@PathVariable UUID sellerId) {
        User user = userService.getById(sellerId);
        if (!user.isSeller()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cet utilisateur n'est pas un vendeur"));
        }

        SellerProfile profile = sellerProfileRepository.findByUser_Id(sellerId).orElse(null);
        return ResponseEntity.ok(Map.of(
                "user", user,
                "profile", profile
        ));
    }
}
