package com.proofpay.user.api;

import com.proofpay.notification.application.NotificationService;
import com.proofpay.notification.domain.NotificationChannel;
import com.proofpay.security.jwt.JwtService;
import com.proofpay.user.application.UserService;
import com.proofpay.user.domain.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final NotificationService notificationService;

    public AuthController(UserService userService, JwtService jwtService, NotificationService notificationService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.notificationService = notificationService;
    }

    @PostMapping("/request-otp")
    public ResponseEntity<Map<String, String>> requestOtp(@Valid @RequestBody OtpRequest request) {
        // 1. Générer l'OTP (le code est stocké en mémoire dans OtpService)
        //    Cette méthode crée l'utilisateur s'il n'existe pas
        String code = userService.requestOtp(request.phone());

        // 2. Récupérer l'utilisateur (existant ou créé)
        //    Utiliser la même méthode que requestOtp pour éviter les problèmes de normalisation
        User user = userService.getByPhone(request.phone());

        // 3. Envoyer le code par SMS via NotificationService avec l'ID de l'utilisateur
        if (user != null && user.getId() != null) {
            notificationService.notifySync(
                    user.getId(),
                    null,
                    NotificationChannel.SMS,
                    "OTP_LOGIN",
                    request.phone(),
                    "Votre code OTP ProofPay est : " + code
            );
        } else {
            // Fallback : envoyer sans userId (si la DB le permet)
            System.err.println("⚠️ Utilisateur introuvable pour : " + request.phone());
        }

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