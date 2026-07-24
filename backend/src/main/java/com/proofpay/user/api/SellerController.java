package com.proofpay.user.api;

import com.proofpay.common.exception.BusinessException;
import com.proofpay.common.util.PhoneNormalizer;
import com.proofpay.user.application.UserService;
import com.proofpay.user.domain.User;
import com.proofpay.user.domain.UserRole;
import com.proofpay.user.domain.UserStatus;
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

    public SellerController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * Inscription d'un nouveau vendeur avec profil complet
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerSeller(@Valid @RequestBody SellerRegistrationRequest request) {
        String phone = PhoneNormalizer.normalize(request.phone());

        // Vérifier si l'utilisateur existe déjà
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
                .role(UserRole.SELLER)
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

        // TODO: Créer SellerProfile et Subscription dans les prochaines étapes

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Vendeur enregistré avec succès. En attente de vérification.",
                "userId", user.getId(),
                "status", user.getStatus().name()
        ));
    }

    /**
     * Recherche de vendeurs par téléphone ou email
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchSellers(@RequestParam(required = false) String phone,
                                            @RequestParam(required = false) String email) {
        if (phone != null && !phone.isEmpty()) {
            String normalizedPhone = PhoneNormalizer.normalize(phone);
            return userRepository.findByPhone(normalizedPhone)
                    .filter(User::isSeller)
                    .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                    .map(user -> ResponseEntity.ok(toSellerPublicProfile(user)))
                    .orElse(ResponseEntity.notFound().build());
        }

        if (email != null && !email.isEmpty()) {
            return userRepository.findByEmail(email)
                    .filter(User::isSeller)
                    .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                    .map(user -> ResponseEntity.ok(toSellerPublicProfile(user)))
                    .orElse(ResponseEntity.notFound().build());
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Veuillez fournir un téléphone ou un email"));
    }

    /**
     * Obtenir le profil public d'un vendeur
     */
    @GetMapping("/{sellerId}/public")
    public ResponseEntity<?> getPublicProfile(@PathVariable UUID sellerId) {
        User user = userService.getById(sellerId);
        if (!user.isSeller() || user.getStatus() != UserStatus.ACTIVE) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toSellerPublicProfile(user));
    }

    /**
     * Vérifier si un numéro est enregistré comme vendeur
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkSeller(@RequestParam String phone) {
        String normalizedPhone = PhoneNormalizer.normalize(phone);
        boolean exists = userRepository.findByPhone(normalizedPhone)
                .map(User::isSeller)
                .orElse(false);
        return ResponseEntity.ok(Map.of("isSeller", exists));
    }

    private SellerPublicProfile toSellerPublicProfile(User user) {
        return new SellerPublicProfile(
                user.getId(),
                user.getDisplayName() != null ? user.getDisplayName() : user.getFirstName() + " " + user.getLastName(),
                user.getPhone(),
                user.getEmail(),
                user.getRating(),
                user.getTransactionsCount() != null ? user.getTransactionsCount() : 0,
                user.isVerifiedSeller(),
                user.getStatus() == UserStatus.ACTIVE
        );
    }

    // ========== RECORDS ==========

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
            String description
    ) {}

    public record SellerPublicProfile(
            UUID id,
            String displayName,
            String phone,
            String email,
            BigDecimal rating,
            int transactionsCount,
            boolean verified,
            boolean active
    ) {}
}
