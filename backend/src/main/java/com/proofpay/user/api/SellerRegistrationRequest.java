package com.proofpay.user.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SellerRegistrationRequest(
        // Compte utilisateur
        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        String phone,

        @Email(message = "Email invalide")
        String email,

        @NotBlank(message = "Le prénom est obligatoire")
        String firstName,

        @NotBlank(message = "Le nom est obligatoire")
        String lastName,

        // Informations professionnelles
        @NotBlank(message = "Le nom de l'entreprise est obligatoire")
        String businessName,

        String businessType,
        String registrationNumber,
        String taxId,
        String businessAddress,
        String businessPhone,
        String businessEmail,
        String website,
        String description,

        // Documents
        String idDocumentUrl,
        String registrationDocumentUrl,

        // Abonnement
        @NotNull(message = "Le plan d'abonnement est obligatoire")
        String subscriptionPlan
) {}
