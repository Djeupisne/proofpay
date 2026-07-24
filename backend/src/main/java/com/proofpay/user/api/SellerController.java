package com.proofpay.user.api;

import com.proofpay.common.exception.BusinessException;
import com.proofpay.common.util.PhoneNormalizer;
import com.proofpay.subscription.domain.Subscription;
import com.proofpay.subscription.domain.SubscriptionPlan;
import com.proofpay.subscription.infrastructure.SubscriptionRepository;
import com.proofpay.user.application.UserService;
import com.proofpay.user.domain.SellerProfile;
import com.proofpay.user.domain.User;
import com.proofpay.user.domain.UserRole;
import com.proofpay.user.domain.UserStatus;
import com.proofpay.user.infrastructure.SellerProfileRepository;
import com.proofpay.user.infrastructure.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sellers")
public class SellerController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final SubscriptionRepository subscriptionRepository;

    public SellerController(UserService userService,
                            UserRepository userRepository,
                            SellerProfileRepository sellerProfileRepository,
                            SubscriptionRepository subscriptionRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerSeller(@Valid @RequestBody SellerRegistrationRequest request) {
        String phone = PhoneNormalizer.normalize(request.phone());

        if (userRepository.findByPhone(phone).isPresent()) {
            throw new BusinessException("USER_EXISTS", "Ce numéro est déjà utilisé.");
        }

        // 1. Créer l'utilisateur vendeur
        User user = User.builder()
                .phone(phone)
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .displayName(request.businessName())
                .status(UserStatus.PENDING_VERIFICATION)
                .role(UserRole.SELLER)  // ✅ Utiliser UserRole.SELLER
                .isSeller(true)
                .isBuyer(false)
                .verifiedSeller(false)
                .approvedSeller(false)
                .preferredLanguage("fr")
                .transactionsCount(0)
                .disputesOpenedCount(0)
                .disputesLostCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        user = userRepository.save(user);

        // 2. Créer le profil vendeur (à implémenter)
        // 3. Créer l'abonnement
        SubscriptionPlan plan = SubscriptionPlan.valueOf(request.subscriptionPlan().toUpperCase());
        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .startDate(Instant.now())
                .endDate(Instant.now().plusSeconds(30 * 24 * 60 * 60))
                .autoRenew(false)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        subscriptionRepository.save(subscription);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Vendeur enregistré avec succès. En attente de vérification.",
                "userId", user.getId(),
                "subscriptionPlan", plan.name()
        ));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchSellers(@RequestParam(required = false) String phone,
                                            @RequestParam(required = false) String email) {
        if (phone != null && !phone.isEmpty()) {
            String normalizedPhone = PhoneNormalizer.normalize(phone);
            return userRepository.findByPhone(normalizedPhone)
                    .filter(User::isSeller)
                    .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                    .map(user -> ResponseEntity.ok(userService.sellerPublicProfileOf(user.getId())))
                    .orElse(ResponseEntity.notFound().build());
        }

        if (email != null && !email.isEmpty()) {
            return userRepository.findByEmail(email)  // ✅ Maintenant disponible
                    .filter(User::isSeller)
                    .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                    .map(user -> ResponseEntity.ok(userService.sellerPublicProfileOf(user.getId())))
                    .orElse(ResponseEntity.notFound().build());
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Veuillez fournir un téléphone ou un email"));
    }

    @GetMapping("/{sellerId}/public")
    public ResponseEntity<?> getPublicProfile(@PathVariable UUID sellerId) {
        User user = userService.getById(sellerId);
        if (!user.isSeller() || user.getStatus() != UserStatus.ACTIVE) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userService.sellerPublicProfileOf(sellerId));
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkSeller(@RequestParam String phone) {
        String normalizedPhone = PhoneNormalizer.normalize(phone);
        return userRepository.findByPhone(normalizedPhone)
                .map(user -> ResponseEntity.ok(Map.of(
                        "isSeller", user.isSeller(),
                        "isActive", user.getStatus() == UserStatus.ACTIVE,
                        "isVerified", user.isVerifiedSeller(),
                        "displayName", user.getDisplayName()
                )))
                .orElse(ResponseEntity.ok(Map.of("isSeller", false)));
    }

    public record SellerRegistrationRequest(
            @NotBlank(message = "Le numéro de téléphone est obligatoire") String phone,
            @Email(message = "Email invalide") String email,
            @NotBlank(message = "Le prénom est obligatoire") String firstName,
            @NotBlank(message = "Le nom est obligatoire") String lastName,
            @NotBlank(message = "Le nom de l'entreprise est obligatoire") String businessName,
            String businessType,
            String registrationNumber,
            String taxId,
            String businessAddress,
            String businessPhone,
            String businessEmail,
            String website,
            String description,
            String idDocumentUrl,
            String registrationDocumentUrl,
            @NotNull(message = "Le plan d'abonnement est obligatoire") String subscriptionPlan
    ) {}
}
