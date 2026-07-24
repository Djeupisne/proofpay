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

    @GetMapping
    public ResponseEntity<List<User>> getAllSellers() {
        return ResponseEntity.ok(userRepository.findByIsSellerTrue());  // ✅ Maintenant disponible
    }

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

        return ResponseEntity.ok(Map.of("message", "Vendeur approuvé avec succès"));
    }

    @PutMapping("/{sellerId}/reject")
    public ResponseEntity<?> rejectSeller(@PathVariable UUID sellerId) {
        User user = userService.getById(sellerId);
        if (!user.isSeller()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cet utilisateur n'est pas un vendeur"));
        }

        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Vendeur rejeté"));
    }

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
