package com.proofpay.user.api;

import com.proofpay.security.util.SecurityUtils;
import com.proofpay.user.application.UserService;
import com.proofpay.user.domain.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public User me() {
        return userService.getById(SecurityUtils.currentUserId());
    }

    /** Profil public réduit d'un autre utilisateur (nom, vérifié, réputation) — voir UserService. */
    @GetMapping("/{id}/public-profile")
    public UserService.PublicProfile publicProfile(@PathVariable UUID id) {
        return userService.publicProfileOf(id);
    }

    /**
     * Complète le profil (§8.1 : "nom, prénom, numéro, photo facultative, langue,
     * canal de notification préféré") — ces champs existaient dans le schéma
     * depuis le début mais n'étaient renseignables nulle part.
     */
    @PutMapping("/me")
    public User updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(SecurityUtils.currentUserId(), request.firstName(),
                request.lastName(), request.email(), request.preferredLanguage());
    }

    public record UpdateProfileRequest(
            String firstName,
            String lastName,
            @Email(message = "Adresse e-mail invalide") String email,
            String preferredLanguage) {
    }
}
