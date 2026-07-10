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

        // 2. Créer ou récupérer l'utilisateur AVANT de générer l'OTP
        User user = userRepository.findByPhone(phone)
                .orElseGet(() -> {
                    System.out.println("📝 Création d'un nouvel utilisateur pour : " + phone);
                    return userRepository.save(User.builder()
                            .phone(phone)
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

        System.out.println("✅ Utilisateur trouvé/créé : " + user.getId() + " pour " + phone);

        // 3. Générer l'OTP
        String code = otpService.generate(phone);

        // 4. Envoyer le SMS avec l'ID de l'utilisateur
        notificationService.notifySync(
                user.getId(), // ✅ userId existe maintenant !
                null,
                NotificationChannel.SMS,
                "OTP_LOGIN",
                phone,
                "Votre code OTP ProofPay est : " + code
        );

        return ResponseEntity.ok(Map.of("message", "OTP envoyé par SMS"));
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

    public record OtpRequest(@NotBlank(message = "Le numéro de téléphone est obligatoire") String phone) {
    }

    public record VerifyOtpRequest(
            @NotBlank(message = "Le numéro de téléphone est obligatoire") String phone,
            @NotBlank(message = "Le code OTP est obligatoire") String code) {
    }
}