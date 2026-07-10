package com.proofpay.notification.provider;

import org.springframework.stereotype.Component;

import java.util.UUID;

/** Implémentation MVP : log console. À remplacer par Firebase Cloud Messaging ou équivalent. */
@Component
public class MockPushSender implements NotificationSender {

    @Override
    public String getChannel() {
        return "PUSH";
    }

    @Override
    public String send(String destination, String templateCode, String renderedMessage) {
        System.out.printf("[PUSH -> %s] (%s) %s%n", destination, templateCode, renderedMessage);
        return "PUSH-" + UUID.randomUUID();
    }
}
