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
                        .preferredChannel(NotificationChannel.SMS) // 🔥 Canal par défaut
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

    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    /** Résout un vendeur par numéro de téléphone (§8.2). */
    public User getByPhone(String rawPhone) {
        String phone = PhoneNormalizer.normalize(rawPhone);
        if (!PhoneNormalizer.isValid(phone)) {
            throw new BusinessException("INVALID_PHONE", "Numéro de téléphone du vendeur invalide");
        }
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucun compte ProofPay n'est associé à ce numéro. Le vendeur doit d'abord s'inscrire."));
    }

    // 🔥 NOUVELLE MÉTHODE : Mettre à jour le canal préféré
    public User updatePreferredChannel(UUID userId, NotificationChannel channel) {
        User user = getById(userId);
        if (channel == null) {
            throw new BusinessException("INVALID_CHANNEL", "Le canal de notification est invalide");
        }
        user.setPreferredChannel(channel);
        user.setUpdatedAt(Instant.now());
        return userRepository.save(user);
    }

    // 🔥 NOUVELLE MÉTHODE : Obtenir le canal préféré d'un utilisateur
    public NotificationChannel getPreferredChannel(UUID userId) {
        User user = getById(userId);
        return user.getPreferredChannel() != null ? user.getPreferredChannel() : NotificationChannel.SMS;
    }

    /** Règle métier #14 et #22 : suspension manuelle ou automatique d'un compte. */
    public void suspend(UUID userId) {
        User user = getById(userId);
        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);
    }

    /** Gestion des rôles par un administrateur déjà en place (cf. AdminController). */
    public User updateRole(UUID userId, UserRole role) {
        User user = getById(userId);
        user.setRole(role);
        return userRepository.save(user);
    }

    /** Complète le profil (§8.1) — champs facultatifs, ignorés s'ils sont null. */
    public User updateProfile(UUID userId, String firstName, String lastName, String email, String preferredLanguage) {
        User user = getById(userId);
        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (email != null) user.setEmail(email);
        if (preferredLanguage != null) user.setPreferredLanguage(preferredLanguage);
        user.setUpdatedAt(Instant.now());
        return userRepository.save(user);
    }

    /**
     * Profil public réduit d'un utilisateur (§8.5 personas).
     */
    public PublicProfile publicProfileOf(UUID userId) {
        User user = getById(userId);
        String name = user.getDisplayName() != null ? user.getDisplayName()
                : (user.getFirstName() != null ? user.getFirstName() + " " + nullToEmpty(user.getLastName()) : "Utilisateur ProofPay");
        return new PublicProfile(
                user.getId(),
                name.trim(),
                user.getStatus() == UserStatus.ACTIVE,
                user.getRating(),
                user.getTransactionsCount() == null ? 0 : user.getTransactionsCount());
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    public record PublicProfile(UUID id, String displayName, boolean verified,
                                 java.math.BigDecimal rating, int transactionsCount) {}
}
