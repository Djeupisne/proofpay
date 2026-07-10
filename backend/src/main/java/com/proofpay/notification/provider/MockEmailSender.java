package com.proofpay.notification.provider;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockEmailSender implements NotificationSender {

    @Override
    public String getChannel() {
        return "EMAIL";
    }

    @Override
    public String send(String destination, String templateCode, String renderedMessage) {
        System.out.printf("[EMAIL -> %s] (%s) %s%n", destination, templateCode, renderedMessage);
        return "EMAIL-" + UUID.randomUUID();
    }
}
