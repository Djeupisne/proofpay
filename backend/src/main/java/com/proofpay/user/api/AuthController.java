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
        String phone = PhoneNormalizer.normalize(request.phone());
        if (!PhoneNormalizer.isValid(phone)) {
            throw new BusinessException("INVALID_PHONE", "Numéro de téléphone invalide");
        }

        User user = userRepository.findByPhone(phone)
                .orElseGet(() -> {
                    System.out.println("📝 Création d'un nouvel utilisateur pour : " + phone);
                    return userRepository.save(
                            User.builder()
                                    .phone(phone)
                                    .email(request.email())
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
                    );
                });

        if (request.email() != null && !request.email().isEmpty()) {
            user.setEmail(request.email());
            userRepository.save(user);
        }

        NotificationChannel channel = determineChannel(request.channel(), user);
        System.out.println("✅ Utilisateur : " + user.getId() + " | Canal : " + channel);

        String code = otpService.generate(phone);
        sendOtp(user, channel, phone, code);

        return ResponseEntity.ok(Map.of("message", "OTP envoyé par " + channel));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        User user = userService.verifyOtp(request.phone(), request.code());
        String role = user.getRole().name();
        String token = jwtService.generateAccessToken(user.getId(), role);
        return ResponseEntity.ok(Map.of(
                "accessToken", token,
                "userId", user.getId().toString(),
                "role", role,
                "isSeller", String.valueOf(user.isSeller()),
                "isBuyer", String.valueOf(user.isBuyer())
        ));
    }

    // ========== MÉTHODES PRIVÉES ==========

    private NotificationChannel determineChannel(String requestedChannel, User user) {
        if (requestedChannel != null) {
            try {
                return NotificationChannel.valueOf(requestedChannel.toUpperCase());
            } catch (IllegalArgumentException e) {
                return user.getPreferredChannel() != null ? user.getPreferredChannel() : NotificationChannel.SMS;
            }
        }
        return user.getPreferredChannel() != null ? user.getPreferredChannel() : NotificationChannel.SMS;
    }

    private void sendOtp(User user, NotificationChannel channel, String phone, String code) {
        if (channel == NotificationChannel.EMAIL && user.getEmail() != null && !user.getEmail().isEmpty()) {
            String emailBody = "<h1>🔐 Votre code OTP ProofPay</h1>" +
                    "<p>Bonjour,</p>" +
                    "<p>Votre code de connexion est : <strong style='font-size:24px;color:#15803d;'>" + code + "</strong></p>" +
                    "<p>Ce code est valable 5 minutes.</p>" +
                    "<p>Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.</p>" +
                    "<hr><p style='color:#666;font-size:12px;'>© ProofPay - Plateforme d'escrow digital</p>";

            notificationService.notifySync(
                    user.getId(),
                    null,
                    NotificationChannel.EMAIL,
                    "OTP_LOGIN",
                    user.getEmail(),
                    emailBody
            );
        } else {
            notificationService.notifySync(
                    user.getId(),
                    null,
                    NotificationChannel.SMS,
                    "OTP_LOGIN",
                    phone,
                    "Votre code OTP ProofPay est : " + code
            );
        }
    }

    // ========== RECORDS ==========

    public record OtpRequest(
            @NotBlank(message = "Le numéro de téléphone est obligatoire") String phone,
            @Email(message = "Email invalide") String email,
            String channel
    ) {}

    public record VerifyOtpRequest(
            @NotBlank(message = "Le numéro de téléphone est obligatoire") String phone,
            @NotBlank(message = "Le code OTP est obligatoire") String code
    ) {}
}
