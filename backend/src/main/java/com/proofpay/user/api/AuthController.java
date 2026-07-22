package com.proofpay.user.api;

import com.proofpay.common.exception.BusinessException;
import com.proofpay.common.util.PhoneNormalizer;
import com.proofpay.notification.application.NotificationService;
import com.proofpay.notification.domain.NotificationChannel;
import com.proofpay.security.jwt.JwtService;
import com.proofpay.security.otp.OtpService;
import com.proofpay.user.application.UserService;
import com.proofpay.user.domain.User;
import com.proofpay.user.domain.UserRole;
import com.proofpay.user.domain.UserStatus;
import com.proofpay.user.infrastructure.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final NotificationService notificationService;

    public AuthController(UserService userService, UserRepository userRepository, OtpService otpService,
                          JwtService jwtService, NotificationService notificationService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.jwtService = jwtService;
        this.notificationService = notificationService;
    }

    @PostMapping("/request-otp")
    public ResponseEntity<Map<String, String>> requestOtp(@Valid @RequestBody OtpRequest request) {
        // 1. Normaliser le téléphone
        String phone = PhoneNormalizer.normalize(request.phone());
        if (!PhoneNormalizer.isValid(phone)) {
            throw new BusinessException("INVALID_PHONE", "Numéro de téléphone invalide");
        }

        // 2. Créer ou récupérer l'utilisateur
        User user = userRepository.findByPhone(phone)
                .orElseGet(() -> {
                    System.out.println("📝 Création d'un nouvel utilisateur pour : " + phone);
                    return userRepository.save(User.builder()
                            .phone(phone)
                            .email(request.email()) // ✅ Sauvegarde de l'email
                            .status(UserStatus.PENDING_VERIFICATION)
                            .role(UserRole.USER)
                            .preferredLanguage("fr")
                            .transactionsCount(0)
                            .disputesOpenedCount(0)
                            .disputesLostCount(0)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build());
                });

        // 3. Mettre à jour l'email si fourni (et différent de l'existant)
        if (request.email() != null && !request.email().isEmpty()) {
            user.setEmail(request.email());
            userRepository.save(user);
        }

        System.out.println("✅ Utilisateur : " + user.getId() + " pour " + phone + " | Email: " + user.getEmail());

        // 4. Générer l'OTP
        String code = otpService.generate(phone);

        // 5. Déterminer le canal (SMS ou EMAIL)
        NotificationChannel channel = NotificationChannel.SMS; // Par défaut
        if (request.channel() != null) {
            try {
                channel = NotificationChannel.valueOf(request.channel().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Si le canal est invalide, on reste sur SMS
            }
        }

        // 6. Envoyer l'OTP via le canal choisi
        if (channel == NotificationChannel.EMAIL && user.getEmail() != null && !user.getEmail().isEmpty()) {
            // Envoi par EMAIL
            String emailBody = "<h1>🔐 Votre code OTP ProofPay</h1>" +
                    "<p>Bonjour,</p>" +
                    "<p>Votre code de connexion est : <strong style='font-size:24px;color:#15803d;'>" + code + "</strong></p>" +
                    "<p>Ce code est valable 5 minutes.</p>" +
                    "<p>Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.</p>" +
                    "<hr>" +
                    "<p style='color:#666;font-size:12px;'>© ProofPay - Plateforme d'escrow digital</p>";

            notificationService.notifySync(
                    user.getId(),
                    null,
                    NotificationChannel.EMAIL,
                    "OTP_LOGIN",
                    user.getEmail(),
                    emailBody
            );
            return ResponseEntity.ok(Map.of("message", "OTP envoyé par EMAIL à " + user.getEmail()));
        } else {
            // Envoi par SMS
            notificationService.notifySync(
                    user.getId(),
                    null,
                    NotificationChannel.SMS,
                    "OTP_LOGIN",
                    phone,
                    "Votre code OTP ProofPay est : " + code
            );
            return ResponseEntity.ok(Map.of("message", "OTP envoyé par SMS à " + phone));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        User user = userService.verifyOtp(request.phone(), request.code());
        String role = user.getRole().name();
        String token = jwtService.generateAccessToken(user.getId(), role);
        return ResponseEntity.ok(Map.of(
                "accessToken", token,
                "userId", user.getId().toString(),
                "role", role
        ));
    }

    // 🔥 NOUVEAU RECORD avec email et channel
    public record OtpRequest(
            @NotBlank(message = "Le numéro de téléphone est obligatoire") String phone,
            @Email(message = "Email invalide") String email,
            String channel // "SMS" ou "EMAIL"
    ) {
    }

    public record VerifyOtpRequest(
            @NotBlank(message = "Le numéro de téléphone est obligatoire") String phone,
            @NotBlank(message = "Le code OTP est obligatoire") String code) {
    }
}
