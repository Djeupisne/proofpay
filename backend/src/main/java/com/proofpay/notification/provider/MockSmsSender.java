package com.proofpay.notification.provider;

import org.springframework.stereotype.Component;

import java.util.UUID;

/** Implémentation MVP : log console. À remplacer par un vrai agrégateur SMS. */
@Component
public class MockSmsSender implements NotificationSender {

    @Override
    public String getChannel() {
        return "SMS";
    }

    @Override
    public String send(String destination, String templateCode, String renderedMessage) {
        System.out.printf("[SMS -> %s] (%s) %s%n", destination, templateCode, renderedMessage);
        return "SMS-" + UUID.randomUUID();
    }
}
