package com.proofpay.user.api;

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

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/request-otp")
    public ResponseEntity<Map<String, String>> requestOtp(@Valid @RequestBody OtpRequest request) {
        // En prod : envoyer le code via NotificationService (SMS). Ici renvoyé pour le MVP/démo.
        String code = userService.requestOtp(request.phone());
        return ResponseEntity.ok(Map.of("message", "OTP envoyé", "debugCode", code));
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

    // ⚠️ @NotBlank ne suffit pas à valider un FORMAT (une chaîne "a" passe le test).
    // La validation stricte du format (regex, longueur) est faite dans
    // PhoneNormalizer.isValid(), appelé par UserService — @NotBlank ici ne sert
    // qu'à rejeter immédiatement (400) les champs vides/manquants avant même
    // d'atteindre le service, ce qui nécessite que ce record soit annoté @Valid
    // dans le contrôleur (sinon ces contraintes sont silencieusement ignorées).
    public record OtpRequest(@NotBlank(message = "Le numéro de téléphone est obligatoire") String phone) {
    }

    public record VerifyOtpRequest(
            @NotBlank(message = "Le numéro de téléphone est obligatoire") String phone,
            @NotBlank(message = "Le code OTP est obligatoire") String code) {
    }
}
