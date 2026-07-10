package com.proofpay.notification.provider;

import com.africastalking.AfricasTalking;
import com.africastalking.SmsService;
import com.africastalking.sms.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary  // 🔥 Utilisé par défaut
public class SmsSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(SmsSender.class);
    private final SmsService smsService;

    public SmsSender(
            @Value("${africastalking.api-key}") String apiKey,
            @Value("${africastalking.username}") String username) {
        this.smsService = AfricasTalking.getService(AfricasTalking.SERVICE_SMS, username, apiKey);
    }

    @Override
    public String getChannel() {
        return "SMS";
    }

    @Override
    public String send(String destination, String templateCode, String renderedMessage) throws Exception {
        try {
            log.info("Envoi SMS à {} : {}", destination, renderedMessage);
            List<Recipient> response = smsService.send(renderedMessage, destination);
            if (response != null && !response.isEmpty()) {
                Recipient recipient = response.get(0);
                log.info("SMS envoyé avec succès : {}", recipient.status);
                return recipient.status;
            }
            throw new Exception("Échec de l'envoi SMS : réponse vide");
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du SMS", e);
            throw new Exception("Erreur lors de l'envoi du SMS", e);
        }
    }
}