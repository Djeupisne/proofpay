package com.proofpay.user.application;

import com.proofpay.common.exception.BusinessException;
import com.proofpay.common.exception.ResourceNotFoundException;
import com.proofpay.common.util.PhoneNormalizer;
import com.proofpay.notification.domain.NotificationChannel;
import com.proofpay.security.otp.OtpService;
import com.proofpay.user.domain.User;
import com.proofpay.user.domain.UserRole;
import com.proofpay.user.domain.UserStatus;
import com.proofpay.user.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final Set<String> bootstrapAdminPhones;

    public UserService(UserRepository userRepository, OtpService otpService,
                        @Value("${proofpay.admin.bootstrap-phones:}") String bootstrapAdminPhonesCsv) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.bootstrapAdminPhones = Arrays.stream(bootstrapAdminPhonesCsv.split(","))
                .map(PhoneNormalizer::normalize)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.toSet());
    }

    // ========== AUTHENTIFICATION ==========

    /** Inscription/connexion par numéro de téléphone (§8.1 spécifications fonctionnelles). */
    public String requestOtp(String rawPhone) {
        String phone = PhoneNormalizer.normalize(rawPhone);
        if (!PhoneNormalizer.isValid(phone)) {
            throw new BusinessException("INVALID_PHONE", "Numéro de téléphone invalide (8 à 15 chiffres attendus)");
        }
        userRepository.findByPhone(phone).orElseGet(() -> userRepository.save(
                User.builder()
                        .phone(phone)
                        .status(UserStatus.PENDING_VERIFICATION)
                        .role(UserRole.USER)
                        .preferredLanguage("fr")
                        .preferredChannel(NotificationChannel.SMS)
                        .isSeller(false)
                        .isBuyer(true)
                        .transactionsCount(0)
                        .disputesOpenedCount(0)
                        .disputesLostCount(0)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build()
        ));
        return otpService.generate(phone);
    }

    public User verifyOtp(String rawPhone, String code) {
        String phone = PhoneNormalizer.normalize(rawPhone);
        if (!PhoneNormalizer.isValid(phone)) {
            throw new BusinessException("INVALID_PHONE", "Numéro de téléphone invalide");
        }
        if (!otpService.verify(phone, code)) {
            throw new BusinessException("OTP_INVALID", "Code OTP invalide ou expiré");
        }
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        user.setStatus(UserStatus.ACTIVE);
        user.setLastLoginAt(Instant.now());

        // Promotion automatique si le numéro fait partie de la liste de bootstrap
        if (bootstrapAdminPhones.contains(phone) && user.getRole() != UserRole.ADMIN) {
            user.setRole(UserRole.ADMIN);
        }

        return userRepository.save(user);
    }

    // ========== RECHERCHE UTILISATEURS ==========

    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    public User getByPhone(String rawPhone) {
        String phone = PhoneNormalizer.normalize(rawPhone);
        if (!PhoneNormalizer.isValid(phone)) {
            throw new BusinessException("INVALID_PHONE", "Numéro de téléphone invalide");
        }
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun compte ProofPay n'est associé à ce numéro."));
    }

    // ========== GESTION VENDEURS ==========

    /**
     * Inscription d'un nouveau vendeur avec profil complet
     */
    public User registerSeller(String phone, String email, String firstName, String lastName, 
                                String businessName, String businessType) {
        // Vérifier si le numéro existe déjà
        if (userRepository.findByPhone(phone).isPresent()) {
            throw new BusinessException("USER_EXISTS", "Ce numéro est déjà utilisé.");
        }

        User user = User.builder()
                .phone(phone)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .displayName(businessName)
                .status(UserStatus.PENDING_VERIFICATION)
                .role(UserRole.SELLER)
                .isSeller(true)
                .isBuyer(false)
                .verifiedSeller(false)
                .approvedSeller(false)
                .preferredLanguage("fr")
                .preferredChannel(NotificationChannel.SMS)
                .transactionsCount(0)
                .disputesOpenedCount(0)
                .disputesLostCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        return userRepository.save(user);
    }

    /**
     * Vérifier si un numéro est enregistré comme vendeur
     */
    public boolean isSeller(String phone) {
        String normalizedPhone = PhoneNormalizer.normalize(phone);
        return userRepository.findByPhone(normalizedPhone)
                .map(User::isSeller)
                .orElse(false);
    }

    /**
     * Vérifier si un vendeur est actif et vérifié
     */
    public boolean isActiveSeller(UUID userId) {
        User user = getById(userId);
        return user.isSeller() && user.getStatus() == UserStatus.ACTIVE && user.isVerifiedSeller();
    }

    /**
     * Approuver un vendeur (par un administrateur)
     */
    public User approveSeller(UUID userId) {
        User user = getById(userId);
        if (!user.isSeller()) {
            throw new BusinessException("NOT_SELLER", "Cet utilisateur n'est pas un vendeur");
        }
        user.setVerifiedSeller(true);
        user.setApprovedSeller(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedAt(Instant.now());
        return userRepository.save(user);
    }

    // ========== GESTION DES CANAUX ==========

    public User updatePreferredChannel(UUID userId, NotificationChannel channel) {
        User user = getById(userId);
        if (channel == null) {
            throw new BusinessException("INVALID_CHANNEL", "Le canal de notification est invalide");
        }
        user.setPreferredChannel(channel);
        user.setUpdatedAt(Instant.now());
        return userRepository.save(user);
    }

    public NotificationChannel getPreferredChannel(UUID userId) {
        User user = getById(userId);
        return user.getPreferredChannel() != null ? user.getPreferredChannel() : NotificationChannel.SMS;
    }

    // ========== GESTION DES COMPTES ==========

    public void suspend(UUID userId) {
        User user = getById(userId);
        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);
    }

    public User updateRole(UUID userId, UserRole role) {
        User user = getById(userId);
        user.setRole(role);
        return userRepository.save(user);
    }

    public User updateProfile(UUID userId, String firstName, String lastName, String email, String preferredLanguage) {
        User user = getById(userId);
        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (email != null) user.setEmail(email);
        if (preferredLanguage != null) user.setPreferredLanguage(preferredLanguage);
        user.setUpdatedAt(Instant.now());
        return userRepository.save(user);
    }

    // ========== PROFIL PUBLIC ==========

    public PublicProfile publicProfileOf(UUID userId) {
        User user = getById(userId);
        String name = user.getDisplayName() != null ? user.getDisplayName()
                : (user.getFirstName() != null ? user.getFirstName() + " " + nullToEmpty(user.getLastName()) : "Utilisateur ProofPay");
        return new PublicProfile(
                user.getId(),
                name.trim(),
                user.getStatus() == UserStatus.ACTIVE,
                user.getRating(),
                user.getTransactionsCount() == null ? 0 : user.getTransactionsCount(),
                user.isSeller(),
                user.isVerifiedSeller(),
                user.isApprovedSeller()
        );
    }

    /**
     * Profil public spécifique pour les vendeurs (avec plus d'informations)
     */
    public SellerPublicProfile sellerPublicProfileOf(UUID userId) {
        User user = getById(userId);
        if (!user.isSeller()) {
            throw new BusinessException("NOT_SELLER", "Cet utilisateur n'est pas un vendeur");
        }
        return new SellerPublicProfile(
                user.getId(),
                user.getDisplayName() != null ? user.getDisplayName() : user.getFirstName() + " " + user.getLastName(),
                user.getPhone(),
                user.getEmail(),
                user.getRating(),
                user.getTransactionsCount() == null ? 0 : user.getTransactionsCount(),
                user.isVerifiedSeller(),
                user.isApprovedSeller(),
                user.getStatus() == UserStatus.ACTIVE,
                user.getCreatedAt()
        );
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    // ========== RECORDS ==========

    public record PublicProfile(
            UUID id, 
            String displayName, 
            boolean verified,
            java.math.BigDecimal rating, 
            int transactionsCount,
            boolean isSeller,
            boolean isVerifiedSeller,
            boolean isApprovedSeller
    ) {}

    public record SellerPublicProfile(
            UUID id,
            String displayName,
            String phone,
            String email,
            java.math.BigDecimal rating,
            int transactionsCount,
            boolean isVerified,
            boolean isApproved,
            boolean isActive,
            Instant createdAt
    ) {}
}
