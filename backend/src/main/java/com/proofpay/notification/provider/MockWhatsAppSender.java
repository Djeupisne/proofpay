package com.proofpay.notification.provider;

import org.springframework.stereotype.Component;

import java.util.UUID;

/** Implémentation MVP : log console. À remplacer par l'API WhatsApp Business. */
@Component
public class MockWhatsAppSender implements NotificationSender {

    @Override
    public String getChannel() {
        return "WHATSAPP";
    }

    @Override
    public String send(String destination, String templateCode, String renderedMessage) {
        System.out.printf("[WHATSAPP -> %s] (%s) %s%n", destination, templateCode, renderedMessage);
        return "WHATSAPP-" + UUID.randomUUID();
    }
}
