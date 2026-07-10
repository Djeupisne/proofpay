package com.proofpay.notification.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Component
@Primary
public class SmsSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(SmsSender.class);
    private final String apiKey;
    private final String apiUrl = "https://api.sms.to/sms/send";
    private final RestTemplate restTemplate;

    public SmsSender(@Value("${sms.to.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getChannel() {
        return "SMS";
    }

    @Override
    public String send(String destination, String templateCode, String renderedMessage) throws Exception {
        try {
            log.info("Envoi SMS à {} : {}", destination, renderedMessage);

            // Préparer la requête
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("message", renderedMessage);
            body.put("to", destination);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            // Envoyer la requête
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    apiUrl,
                    request,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                log.info("SMS envoyé avec succès : {}", responseBody);
                return responseBody.get("message_id").toString();
            } else {
                throw new Exception("Échec de l'envoi SMS : " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du SMS", e);
            throw new Exception("Erreur lors de l'envoi du SMS", e);
        }
    }
}