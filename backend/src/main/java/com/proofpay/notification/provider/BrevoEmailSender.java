package com.proofpay.notification.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Component
public class BrevoEmailSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(BrevoEmailSender.class);
    private final String apiKey;
    private final String fromEmail;
    private final String fromName;
    private final String apiUrl = "https://api.brevo.com/v3/smtp/email";
    private final RestTemplate restTemplate;

    public BrevoEmailSender(
            @Value("${brevo.api-key}") String apiKey,
            @Value("${brevo.from-email}") String fromEmail,
            @Value("${brevo.from-name:ProofPay}") String fromName) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.restTemplate = new RestTemplate();
        log.info("✅ BrevoEmailSender initialisé avec: fromEmail={}", fromEmail);
    }

    @Override
    public String getChannel() {
        return "EMAIL";
    }

    @Override
    public String send(String destination, String templateCode, String renderedMessage) throws Exception {
        try {
            log.info("📧 Envoi email à {} : {}", destination, renderedMessage);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            Map<String, Object> body = new HashMap<>();
            
            // Expéditeur
            Map<String, Object> sender = new HashMap<>();
            sender.put("email", fromEmail);
            sender.put("name", fromName);
            body.put("sender", sender);

            // Destinataire
            Map<String, Object> to = new HashMap<>();
            to.put("email", destination);
            body.put("to", new Map[]{to});

            // Sujet et contenu
            body.put("subject", "🔐 Votre code OTP ProofPay");
            body.put("htmlContent", renderedMessage);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                log.info("✅ Email envoyé avec succès : {}", responseBody);
                return responseBody.get("messageId").toString();
            } else {
                throw new Exception("Échec de l'envoi email : " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi de l'email", e);
            throw new Exception("Erreur lors de l'envoi de l'email", e);
        }
    }
}
